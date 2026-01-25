// controller/GameController.java
package controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import model.Board;
import model.Cell;
import model.GameHistoryEntry;
import model.GameModel;
import model.GameModelObserver;
import model.Question;
import model.QuestionDifficulty;
import model.SysData;
import multiplayer.GameAction;
import multiplayer.GameOverPayload;
import multiplayer.MenuAction;
import multiplayer.MpMessage;
import multiplayer.MpMessageType;
import multiplayer.MultiplayerDispatcher;
import multiplayer.MultiplayerSession;
import view.GameView;

import service.RevealService;
import service.SpecialAction;
import service.SpecialCellResult;
import service.SpecialCellService;

public class GameController implements GameModelObserver {

    private static GameController instance;

    public int N, M;

    public GameView gameView;
    public GameModel gameModel;

    public CellController[][] board1;
    public CellController[][] board2;

    public String player1Name = "Player 1";
    public String player2Name = "Player 2";

    private int currentPlayer = 1;
    private boolean gameActive = true;

    // Prevent endGame running twice (fixes double save: WIN + LOSE)
    private boolean endGameTriggered = false;

    // ✅ prevent showing end game twice
    private boolean endGameDialogShown = false;

    private Timer gameTimer;
    private int elapsedTime = 0;

    private String difficulty;
    private int mineCount;
    private int sharedLives;

    private final Stage primaryStage;

    // ✅ seeded RNG
    private final Random rng;
    private final long seed;

    private final RevealService revealService = new RevealService();
    private final SpecialCellService specialCellService;

    private BoardController board1Controller;
    private BoardController board2Controller;

    private MultiplayerDispatcher mpDispatcher; // offline by default

    // Multiplayer hooks
    private MultiplayerSession multiplayerSession;
    private boolean multiplayerEnabled = false;
    
    // ✅ which player am I on THIS PC?
    // Host = 1, Client = 2 (in multiplayer). Offline uses -1.
    private int localPlayerNum = -1;


    public static GameController getInstance(String difficulty, String p1Name, String p2Name, Stage stage) {
        return getInstance(difficulty, p1Name, p2Name, stage, System.nanoTime());
    }

    public static GameController getInstance(String difficulty, String p1Name, String p2Name, Stage stage, long seed) {
        if (instance == null) {
            instance = new GameController(difficulty, p1Name, p2Name, stage, seed);
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    private GameController(String difficulty, String p1Name, String p2Name, Stage stage, long seed) {
        this.primaryStage = stage;
        this.difficulty = difficulty;
        this.player1Name = p1Name;
        this.player2Name = p2Name;

        this.seed = seed;
        this.rng = new Random(seed);
        this.specialCellService = new SpecialCellService(this.rng);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double screenHeight = bounds.getHeight();
        double screenWidth = bounds.getWidth();

        double boardHeightBudget = screenHeight - 360;
        if (boardHeightBudget < 220) {
            boardHeightBudget = 220;
        }

        int baseCellSize;

        switch (difficulty) {
            case "Easy":
                N = M = 9;
                mineCount = 10;
                sharedLives = 10;
                baseCellSize = 36;
                break;

            case "Medium":
                N = M = 13;
                mineCount = 26;
                sharedLives = 8;
                baseCellSize = 28;
                break;

            case "Hard":
            default:
                N = M = 16;
                mineCount = 44;
                sharedLives = 6;
                baseCellSize = 26;
                break;
        }

        int maxByHeight = (int) Math.floor(boardHeightBudget / N);

        double centerAreaWidth = 340;
        double perBoardWidthBudget = (screenWidth - centerAreaWidth) / 2.0;
        if (perBoardWidthBudget < 180) {
            perBoardWidthBudget = 180;
        }
        int maxByWidth = (int) Math.floor(perBoardWidthBudget / M);

        int maxAllowed = Math.min(maxByHeight, maxByWidth);
        int cellSize = Math.min(baseCellSize, maxAllowed);

        if (cellSize > 20) {
            cellSize -= 2;
        }
        cellSize = Math.max(cellSize, 18);

        CellController.setCellSide(cellSize);

        gameModel = new GameModel(this, mineCount, sharedLives, rng);
        gameModel.addObserver(this);

        gameView = new GameView(this);

        mpDispatcher = new MultiplayerDispatcher(this, MultiplayerDispatcher.Role.OFFLINE, null);

        init();
        setupEventHandlers();
        startTimer();
    }

    @Override
    public void onGameModelChanged() {
        Platform.runLater(this::updateUI);
    }

    // ✅ Attach multiplayer session once game starts (host or client)
    public void attachMultiplayer(MultiplayerSession session) {
        this.multiplayerSession = session;
        this.multiplayerEnabled = true;
        this.localPlayerNum = session.isHost() ? 1 : 2;


        session.setOnActionReceived(action -> Platform.runLater(() -> applyRemoteAction(action)));

        // Game Over broadcast
        session.setOnGameOver(payload -> Platform.runLater(() -> endGameFromNetwork(payload)));

        // ✅ ONLY New Game uses approval now
        session.setOnActionRequest(action -> Platform.runLater(() -> {
            // We only support approval for NEW_GAME
            if (action.type != MenuAction.Type.NEW_GAME) {
                session.respondToPendingRequest(false);
                return;
            }

            boolean ok = showConfirmation(
                    "Approval Required",
                    "Player " + action.requestedBy + " wants to start a New Game.\nApprove?",
                    "Approve"
            );
            session.respondToPendingRequest(ok);
        }));

        session.setOnExecuteAction(action -> Platform.runLater(() -> {
            if (action.type == MenuAction.Type.NEW_GAME) {
                executeNewGameNow();
            }
        }));

        session.setOnActionDeclined(type -> Platform.runLater(() -> {
            showMessage("Request Declined", "The other player declined the New Game request.");
        }));

        // ✅ when other leaves -> show message and disable multiplayer locally
        session.setOnPlayerLeft(name -> Platform.runLater(() -> {
            showMessage("Player Left", "Player " + name + " is out now.\nMultiplayer session ended.");
            disableMultiplayerLocally();
        }));
    }

    private void disableMultiplayerLocally() {
        multiplayerEnabled = false;
        // close session safely
        if (multiplayerSession != null) {
            try { multiplayerSession.close(); } catch (Exception ignored) {}
        }
        multiplayerSession = null;
    }

    private void applyRemoteAction(GameAction action) {
        if (action.type == GameAction.Type.LEFT_CLICK) {
            handleLeftClick(action.playerNum, action.row, action.col);
        } else if (action.type == GameAction.Type.RIGHT_CLICK) {
            handleRightClick(action.playerNum, action.row, action.col);
        }
    }

    private void handleLeftClick(int playerNum, int row, int col) {
        BoardController bc = (playerNum == 1) ? board1Controller : board2Controller;
        if (bc == null) return;
        bc.handleLeftClick(row, col);
    }

    private void handleRightClick(int playerNum, int row, int col) {
        BoardController bc = (playerNum == 1) ? board1Controller : board2Controller;
        if (bc == null) return;
        bc.handleRightClick(row, col);
    }

    public void init() {
        gameActive = true;
        endGameTriggered = false;
        endGameDialogShown = false;
        currentPlayer = 1;
        elapsedTime = 0;

        BoardController.resetInstances();

        gameModel.initializeBoards(N, M);

        Board logicalBoard1 = gameModel.getBoard1();
        Board logicalBoard2 = gameModel.getBoard2();

        board1 = createUiBoard(logicalBoard1);
        board2 = createUiBoard(logicalBoard2);

        createCellsGrid(board1, gameView.gridPane1);
        createCellsGrid(board2, gameView.gridPane2);

        board1Controller = BoardController.getInstance(1, this, gameModel, logicalBoard1, board1, revealService);
        board2Controller = BoardController.getInstance(2, this, gameModel, logicalBoard2, board2, revealService);

        addEventHandlersToBoard(board1Controller);
        addEventHandlersToBoard(board2Controller);

        updateUI();
        gameView.initMascotScoreTracking();
        highlightCurrentPlayer();
    }

    private CellController[][] createUiBoard(Board logicalBoard) {
        int rows = logicalBoard.getRows();
        int cols = logicalBoard.getCols();
        CellController[][] uiBoard = new CellController[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = logicalBoard.getCell(r, c);
                uiBoard[r][c] = new CellController(cell);
            }
        }
        return uiBoard;
    }

    private void createCellsGrid(CellController[][] board, javafx.scene.layout.GridPane gridPane) {
        gridPane.getChildren().clear();

        Color boardTint = (gridPane == gameView.gridPane1)
                ? Color.web("#6FAF8F")
                : Color.web("#C26A6A");

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < M; c++) {
                CellController cellCtrl = board[r][c];
                cellCtrl.setBoardTint(boardTint);
                cellCtrl.init();
                gridPane.add(cellCtrl.cellView, c, r);
            }
        }
    }

    private void setupEventHandlers() {

        // ✅ New Game -> approval only if multiplayer enabled
        gameView.restartBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (multiplayerEnabled && multiplayerSession != null) {
                String me = multiplayerSession.isHost() ? player1Name : player2Name;
                multiplayerSession.requestSharedAction(MenuAction.Type.NEW_GAME, me);
                return;
            }

            boolean ok = showConfirmation("Start New Game",
                    "Start a new game?\nCurrent progress will be lost.",
                    "New Game");

            if (!ok) return;

            executeNewGameNow();
        });

        // ✅ Exit -> NO approval (leave immediately)
        gameView.exitBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {

            if (multiplayerEnabled && multiplayerSession != null) {
                // notify other side and close session
                String me = multiplayerSession.isHost() ? player1Name : player2Name;
                try { multiplayerSession.notifyPlayerLeft(me); } catch (Exception ignored) {}
                disableMultiplayerLocally();
            }

            boolean ok = showConfirmation("Exit Game", "Are you sure you want to exit the game?", "Exit");
            if (!ok) return;

            Platform.exit();
            System.exit(0);
        });

        // ✅ Return menu -> NO approval (leave immediately)
        gameView.backToMenuBtn.setOnAction(e -> {

            if (multiplayerEnabled && multiplayerSession != null) {
                String me = multiplayerSession.isHost() ? player1Name : player2Name;
                try { multiplayerSession.notifyPlayerLeft(me); } catch (Exception ignored) {}
                disableMultiplayerLocally();
            }

            boolean ok = showConfirmation("Return to Menu",
                    "Return to the main menu?\nCurrent game progress will be lost.",
                    "Return");

            if (!ok) return;

            if (gameTimer != null) gameTimer.cancel();
            Main.showMainMenu(primaryStage);
        });
    }

    private void executeNewGameNow() {
        if (gameTimer != null) gameTimer.cancel();
        init();
        startTimer();
    }

    private void addEventHandlersToBoard(BoardController bc) {
        CellController[][] board = bc.getUiBoard();
        int playerNum = bc.getPlayerNum();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                int row = i;
                int col = j;

                board[i][j].cellView.setOnMouseClicked(event -> {
                    if (!gameActive || endGameTriggered) return;
                     // ✅ Multiplayer: only allow clicking YOUR board, on YOUR turn
                    if (multiplayerEnabled && multiplayerSession != null) {
                        // block interacting with the other board always
                        if (playerNum != localPlayerNum) return;

                        // only act on your turn
                        if (currentPlayer != localPlayerNum) return;
                    } else {
                        // Offline behavior stays exactly like before
                        if (currentPlayer != playerNum) return;
                    }


                    Cell cell = board[row][col].getCell();

                    if (event.getButton() == MouseButton.PRIMARY) {

                        boolean valid =
                                (!cell.isOpen() && !cell.isFlag())
                                || (cell.isSpecial() && cell.isOpen() && !cell.isActivated());

                        if (!valid) return;

                        mpDispatcher.onLocalLeftClick(playerNum, row, col);

                        if (multiplayerEnabled && multiplayerSession != null) {
                            multiplayerSession.send(new GameAction(GameAction.Type.LEFT_CLICK, playerNum, row, col));
                        }
                    } else if (event.getButton() == MouseButton.SECONDARY) {

                        boolean valid = !cell.isOpen();
                        if (!valid) return;

                        mpDispatcher.onLocalRightClick(playerNum, row, col);

                        if (multiplayerEnabled && multiplayerSession != null) {
                            multiplayerSession.send(new GameAction(GameAction.Type.RIGHT_CLICK, playerNum, row, col));
                        }
                    }
                });
            }
        }
    }

    public boolean isGameActive() {
        return gameActive && !endGameTriggered;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void switchPlayer() {
        if (!isGameActive()) return;

        currentPlayer = (currentPlayer == 1 ? 2 : 1);
        highlightCurrentPlayer();
        updateUI();
    }

    private void highlightCurrentPlayer() {
        gameView.setActivePlayer(currentPlayer);
    }


    public void updateUI() {
    	gameView.setSharedScoreWithReaction(gameModel.getSharedScore());
    	gameView.setSharedLivesWithReaction(gameModel.getSharedLives());
        gameView.currentPlayerLabel.setText((currentPlayer == 1 ? player1Name : player2Name) + "'s Turn");

        gameView.difficultyLabel.setText(difficulty);
        gameView.timeLabel.setText(formatTime(elapsedTime));

        if (board1Controller != null) {
            gameView.player1MinesLeftLabel.setText("Mines Left: " + board1Controller.getMinesLeft());
        }
        if (board2Controller != null) {
            gameView.player2MinesLeftLabel.setText("Mines Left: " + board2Controller.getMinesLeft());
        }

        if (gameModel.getSharedLives() <= 3) {
            gameView.sharedLivesLabel.setTextFill(Color.web("#FCA5A5"));
        } else {
            gameView.sharedLivesLabel.setTextFill(Color.web("#A5B4FC"));
        }
    }

    private String formatTime(int sec) {
        int m = sec / 60;
        int s = sec % 60;
        return String.format("%02d:%02d", m, s);
    }

    public void handleMineHit() {
        if (!isGameActive()) return;

        gameModel.addLives(-1);

        // ✅ mascot reacts sad when mine hit
        gameView.onMineHitMascot();

        showMessage("Mine Hit!",
                "You hit a mine! -1 life.\nShared Lives Remaining: " + gameModel.getSharedLives());

        if (gameModel.getSharedLives() <= 0) {
            endGame(false);
        }
    }

    // -------- keep all your Surprise/Question logic unchanged --------
    // (I’m not removing anything; below is exactly your original code structure)

    public boolean activateSurpriseCell(CellController cellCtrl) {
        if (!isGameActive()) return false;

        Cell cell = cellCtrl.getCell();

        SpecialCellResult res = specialCellService.processSurprise(
                difficulty,
                gameModel.getSharedScore(),
                gameModel.getSharedLives()
        );

        if (!res.allowed) {
            showMessage(res.title, res.message);
            return false;
        }

        gameModel.setSharedScore(res.newScore);
        gameModel.setSharedLives(res.newLives);

        showMessage(res.title, res.message);

        cell.setActivated(true);
        cellCtrl.init();

        if (res.gameOver) {
            endGame(false);
            return false;
        }

        return true;
    }

    public void activateQuestionCell(CellController cellCtrl) {
        if (!isGameActive()) return;

        List<Question> all = SysData.loadQuestions();
        if (all == null || all.isEmpty()) {
            showMessage("No Questions", "No questions found in QuestionsCSV.csv");
            return;
        }

        Question q = all.get(rng.nextInt(all.size()));
        String qDiffLabel = mapDifficultyLabel(q.getDifficulty());

        Optional<Boolean> result = QuestionPopup.show(primaryStage, q, qDiffLabel);

        if (!isGameActive()) return;

        if (result.isPresent()) {
            boolean correct = result.get();

            applyQuestionReward(correct, qDiffLabel);

            Cell cell = cellCtrl.getCell();
            cell.setActivated(true);
            cellCtrl.init();

            if (gameModel.getSharedLives() <= 0) {
                endGame(false);
            }
        }
    }

    private String mapDifficultyLabel(QuestionDifficulty diff) {
        if (diff == null) return "Easy";
        switch (diff) {
            case EASY: return "Easy";
            case MEDIUM: return "Intermediate";
            case HARD: return "Hard";
            case EXPERT: return "Expert";
            default: return "Easy";
        }
    }

    private void applyQuestionReward(boolean correct, String qDiff) {
    	
    	 if (correct) gameView.onQuestionCorrect();
    	    else         gameView.onQuestionWrong();

        SpecialCellResult res = specialCellService.processQuestion(
                difficulty, qDiff, correct,
                gameModel.getSharedScore(),
                gameModel.getSharedLives()
        );

        gameModel.setSharedScore(res.newScore);
        gameModel.setSharedLives(res.newLives);

        StringBuilder extraInfo = new StringBuilder();

        if (res.actions.contains(SpecialAction.MINE_GIFT)) {
            boolean done = revealMineGiftCell();
            if (done) extraInfo.append("\nMine gift: one hidden mine has been marked on your board.");
            else      extraInfo.append("\nMine gift: no hidden mines left to mark.");
        }

        if (res.actions.contains(SpecialAction.REVEAL_AREA_3X3)) {
            int revealed = revealBestAvailableBlockForCurrentPlayer();
            extraInfo.append("\nReveal bonus: ")
                    .append(revealed)
                    .append(" cells have been uncovered.");
        }

        String msg = res.message;
        if (extraInfo.length() > 0) msg += "\n" + extraInfo;

        showMessage(res.title, msg);

        if (res.gameOver) {
            endGame(false);
        }
    }

    private boolean revealMineGiftCell() {

        BoardController bc = (currentPlayer == 1) ? board1Controller : board2Controller;
        CellController[][] uiBoard = (currentPlayer == 1) ? board1 : board2;

        if (bc == null || uiBoard == null) return false;

        class Pos {
            final int row, col;
            Pos(int r, int c) { row = r; col = c; }
        }

        List<Pos> candidates = new ArrayList<>();

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < M; c++) {
                Cell cell = uiBoard[r][c].getCell();
                if (cell.isMine() && !cell.isOpen() && !cell.isFlag()) {
                    candidates.add(new Pos(r, c));
                }
            }
        }

        if (candidates.isEmpty()) return false;

        Pos chosen = candidates.get(rng.nextInt(candidates.size()));
        return bc.applyMineGiftFlag(chosen.row, chosen.col);
    }

    private int revealBestAvailableBlockForCurrentPlayer() {
        CellController[][] board = getCurrentBoard();
        if (board == null) return 0;

        int rows = board.length;
        int cols = board[0].length;

        int[][] shapes = {
                {3,3},
                {3,2}, {2,3},
                {2,2},
                {3,1}, {1,3},
                {2,1}, {1,2},
                {1,1}
        };

        List<int[]> bestCells = null;

        for (int[] shape : shapes) {
            int h = shape[0];
            int w = shape[1];

            for (int r = 0; r <= rows - h; r++) {
                for (int c = 0; c <= cols - w; c++) {

                    List<int[]> cells = new ArrayList<>();

                    for (int i = 0; i < h; i++) {
                        for (int j = 0; j < w; j++) {
                            Cell cell = board[r + i][c + j].getCell();
                            if (!cell.isOpen() && !cell.isFlag()) {
                                cells.add(new int[]{r + i, c + j});
                            }
                        }
                    }

                    if (!cells.isEmpty()) {
                        if (bestCells == null || cells.size() > bestCells.size()) {
                            bestCells = cells;
                        }
                    }
                }
            }

            if (bestCells != null) {
                for (int[] pos : bestCells) {
                    revealCellFromGift(board, pos[0], pos[1]);
                }
                checkWinCondition();
                return bestCells.size();
            }
        }

        return 0;
    }

    private void revealCellFromGift(CellController[][] board, int row, int col) {
        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length) return;

        CellController cellCtrl = board[row][col];
        Cell cell = cellCtrl.getCell();

        if (cell.isOpen() || cell.isFlag()) return;

        cell.setOpen(true);
        gameModel.revealedCells++;

        if (cell.isMine()) {
            BoardController bc = (currentPlayer == 1) ? board1Controller : board2Controller;
            if (bc != null) {
                bc.onMineOpenedByGift();
            }
        }

        if (cell.isSpecial() && !cell.isDiscovered()) {
            cell.setDiscovered(true);
        }

        cellCtrl.init();
    }

    private CellController[][] getCurrentBoard() {
        return (currentPlayer == 1) ? board1 : board2;
    }

    private boolean isAllMinesCorrectlyFlagged(BoardController bc) {
        return bc != null && bc.getMinesLeft() == 0;
    }

    public void checkWinCondition() {
        if (!isGameActive()) return;

        boolean clearedByOpen = isBoardCleared(board1) || isBoardCleared(board2);

        boolean clearedByFlags = isAllMinesCorrectlyFlagged(board1Controller)
                || isAllMinesCorrectlyFlagged(board2Controller);

        if (clearedByOpen || clearedByFlags) {
            endGame(true);
        }
    }

    private boolean isBoardCleared(CellController[][] board) {
        if (board == null) return false;

        int safeCells = 0;
        int openedSafeCells = 0;

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < M; c++) {
                Cell cell = board[r][c].getCell();
                if (!cell.isMine()) {
                    safeCells++;
                    if (cell.isOpen()) openedSafeCells++;
                }
            }
        }

        return safeCells > 0 && openedSafeCells == safeCells;
    }

    private void startTimer() {
        stopTimer();

        gameTimer = new Timer();
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (isGameActive()) {
                        elapsedTime++;
                        updateUI();
                    }
                });
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
    }

    private void endGame(boolean won) {
        if (endGameTriggered) return;

        endGameTriggered = true;
        gameActive = false;

        stopTimer();

        if (board1Controller != null) board1Controller.forceRevealAll();
        if (board2Controller != null) board2Controller.forceRevealAll();

        int lifeBonus = gameModel.getSharedLives() *
                (difficulty.equals("Easy") ? 5 : difficulty.equals("Medium") ? 8 : 12);

        int finalScore = gameModel.getSharedScore() + lifeBonus;

        // Multiplayer: host authoritative
        if (multiplayerEnabled && multiplayerSession != null) {

            if (multiplayerSession.isHost()) {
                multiplayerSession.sendMessage(new MpMessage(
                        MpMessageType.GAME_OVER,
                        new GameOverPayload(won, lifeBonus, finalScore)
                ));

                // Only host saves history
                saveGameToHistory(won, finalScore);

                endGameDialogShown = true;
                showEndGameDialog(won, lifeBonus, finalScore);
            } else {
                // client waits for host payload
            }
            return;
        }

        // Offline
        saveGameToHistory(won, finalScore);
        endGameDialogShown = true;
        showEndGameDialog(won, lifeBonus, finalScore);
    }

    private void endGameFromNetwork(GameOverPayload payload) {
        if (endGameDialogShown) return;

        if (!endGameTriggered) {
            endGameTriggered = true;
            gameActive = false;

            stopTimer();

            if (board1Controller != null) board1Controller.forceRevealAll();
            if (board2Controller != null) board2Controller.forceRevealAll();
        }

        endGameDialogShown = true;
        showEndGameDialog(payload.won, payload.lifeBonus, payload.finalScore);
    }

    private void saveGameToHistory(boolean won, int finalScore) {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String result = won ? "WIN" : "LOSE";

        GameHistoryEntry entry = new GameHistoryEntry(dateTime, difficulty, player1Name, player2Name, result,
                finalScore, elapsedTime);

        SysData.saveGame(entry);
    }

    // ---------------- UI dialogs (unchanged) ----------------

    public void showMessage(String title, String msg) {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.WINDOW_MODAL);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("""
                    -fx-font-size: 18px;
                    -fx-font-weight: bold;
                    -fx-text-fill: white;
                """);

        Button closeX = new Button("✕");
        closeX.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: #93C5FD;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-padding: 4 10;
                """);
        closeX.setOnAction(e -> dialog.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, titleLabel, spacer, closeX);
        header.setAlignment(Pos.CENTER_LEFT);

        Label messageLabel = new Label(msg);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(480);
        messageLabel.setStyle("""
                    -fx-font-size: 14px;
                    -fx-text-fill: #D7E1FF;
                    -fx-line-spacing: 4px;
                """);

        Button ok = new Button("OK ✓");
        ok.setStyle("""
                    -fx-background-color: #4C7DFF;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-padding: 10 32;
                    -fx-background-radius: 14;
                """);
        ok.setOnAction(e -> dialog.close());

        HBox btnBox = new HBox(ok);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(16, header, messageLabel, btnBox);
        card.setPadding(new Insets(20));
        card.setMaxWidth(520);
        card.setStyle("""
                    -fx-background-color: rgba(15, 15, 26, 0.97);
                    -fx-background-radius: 16;
                    -fx-border-radius: 16;
                    -fx-border-color: rgba(76, 125, 255, 0.55);
                    -fx-border-width: 1.2;
                """);
        card.setEffect(new DropShadow(18, Color.color(0, 0, 0, 0.55)));

        Scene scene = new Scene(card);
        scene.setFill(Color.TRANSPARENT);

        dialog.setScene(scene);
        dialog.sizeToScene();
        dialog.centerOnScreen();
        dialog.showAndWait();
    }

    private boolean showConfirmation(String title, String message, String confirmText) {

        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.WINDOW_MODAL);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("""
                    -fx-font-size: 18px;
                    -fx-font-weight: bold;
                    -fx-text-fill: white;
                """);

        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(420);
        msgLabel.setStyle("""
                    -fx-font-size: 14px;
                    -fx-text-fill: #D7E1FF;
                """);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("""
                    -fx-background-color: #64748B;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-padding: 8 26;
                    -fx-background-radius: 14;
                """);

        Button confirmBtn = new Button(confirmText);
        confirmBtn.setStyle("""
                    -fx-background-color: #EF4444;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-padding: 8 26;
                    -fx-background-radius: 14;
                """);

        final boolean[] confirmed = { false };

        cancelBtn.setOnAction(e -> dialog.close());
        confirmBtn.setOnAction(e -> {
            confirmed[0] = true;
            dialog.close();
        });

        HBox buttons = new HBox(12, cancelBtn, confirmBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(16, titleLabel, msgLabel, buttons);
        card.setPadding(new Insets(20));
        card.setStyle("""
                    -fx-background-color: rgba(15, 15, 26, 0.97);
                    -fx-background-radius: 16;
                    -fx-border-radius: 16;
                    -fx-border-color: rgba(239, 68, 68, 0.6);
                    -fx-border-width: 1.2;
                """);

        Scene scene = new Scene(card);
        scene.setFill(Color.TRANSPARENT);

        dialog.setScene(scene);
        dialog.sizeToScene();
        dialog.centerOnScreen();
        dialog.showAndWait();

        return confirmed[0];
    }

    private void showEndGameDialog(boolean won, int lifeBonus, int finalScore) {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.WINDOW_MODAL);

        Label icon = new Label(won ? "✓" : "!");
        icon.setStyle("""
                    -fx-font-size: 26px;
                    -fx-font-weight: bold;
                    -fx-text-fill: %s;
                """.formatted(won ? "#22C55E" : "#F87171"));

        Label titleLabel = new Label(won ? "Victory!" : "Game Over");
        titleLabel.setStyle("""
                    -fx-font-size: 24px;
                    -fx-font-weight: bold;
                    -fx-text-fill: white;
                """);

        HBox titleBox = new HBox(10, icon, titleLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(titleBox);
        header.setAlignment(Pos.CENTER_LEFT);

        Label sub = new Label(won ? "Well done " + player1Name + " & " + player2Name + "!" : "Out of Lives!");
        sub.setStyle("-fx-font-size: 16px; -fx-text-fill: #D7E1FF;");

        String bodyText = "Difficulty: " + difficulty + "\n" +
                "Time: " + formatTime(elapsedTime) + "\n\n" +
                "Base Score: " + gameModel.getSharedScore() + "\n" +
                "Lives Bonus: +" + lifeBonus + "\n" +
                "═══════ FINAL SCORE ═══════\n" + finalScore;

        Label body = new Label(bodyText);
        body.setWrapText(true);
        body.setMaxWidth(560);
        body.setStyle("""
                    -fx-font-size: 16px;
                    -fx-text-fill: #D7E1FF;
                    -fx-line-spacing: 4px;
                """);

        Button newGameBtn = new Button("New Game 🎮");
        newGameBtn.setStyle("""
                    -fx-background-color: #4C7DFF;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-font-size: 15px;
                    -fx-padding: 12 36;
                    -fx-background-radius: 16;
                """);

        // ✅ New Game -> approval only
        newGameBtn.setOnAction(e -> {
            dialog.close();

            if (multiplayerEnabled && multiplayerSession != null) {
                String me = multiplayerSession.isHost() ? player1Name : player2Name;
                multiplayerSession.requestSharedAction(MenuAction.Type.NEW_GAME, me);
                return;
            }

            executeNewGameNow();
        });

        Button menuBtn = new Button("Return to Menu");
        menuBtn.setStyle("""
                    -fx-background-color: #64748B;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-font-size: 15px;
                    -fx-padding: 12 36;
                    -fx-background-radius: 16;
                """);

        // ✅ Return to menu -> NO approval, leave immediately & notify other
        menuBtn.setOnAction(e -> {
            dialog.close();

            if (multiplayerEnabled && multiplayerSession != null) {
                String me = multiplayerSession.isHost() ? player1Name : player2Name;
                try { multiplayerSession.notifyPlayerLeft(me); } catch (Exception ignored) {}
                disableMultiplayerLocally();
            }

            Main.showMainMenu(primaryStage);
        });

        HBox buttons = new HBox(14, newGameBtn, menuBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(18, header, sub, body, buttons);
        card.setPadding(new Insets(26));
        card.setMaxWidth(650);
        card.setStyle("""
                    -fx-background-color: rgba(15, 15, 26, 0.97);
                    -fx-background-radius: 18;
                    -fx-border-radius: 18;
                    -fx-border-color: rgba(76, 125, 255, 0.6);
                    -fx-border-width: 1.5;
                """);
        card.setEffect(new DropShadow(22, Color.color(0, 0, 0, 0.65)));

        Scene scene = new Scene(card);
        scene.setFill(Color.TRANSPARENT);

        dialog.setScene(scene);
        dialog.sizeToScene();
        dialog.centerOnScreen();
        dialog.showAndWait();
    }
    public String getDifficulty() {
        return difficulty;
    }
}
