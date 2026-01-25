// controller/GameController.java
package controller;

import java.time.LocalDateTime;
import java.util.Optional;

import service.LocalGameState;
import service.LocalResumeCodec;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

// ✅ NEW imports
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import multiplayer.GameSettings;
import multiplayer.MenuAction;
import multiplayer.MpMessage;
import multiplayer.MpMessageType;
import multiplayer.MultiplayerDispatcher;
import multiplayer.MultiplayerSession;
// ✅ NEW
import multiplayer.QuestionResultPayload;

import view.GameView;

import service.RevealService;
import service.SoundService;
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
    private Random rng;
    private long seed;
    private SpecialCellService specialCellService;

    // ✅ NEW: New game seed received from host
    private Long pendingNewGameSeed = null;

    
    private final RevealService revealService = new RevealService();
    

    private BoardController board1Controller;
    private BoardController board2Controller;

    private MultiplayerDispatcher mpDispatcher; // offline by default

    // Multiplayer hooks
    private MultiplayerSession multiplayerSession;
    private boolean multiplayerEnabled = false;

    // ✅ which player am I on THIS PC?
    // Host = 1, Client = 2 (in multiplayer). Offline uses -1.
    private int localPlayerNum = -1;

    // ✅ NEW: keep question RNG in sync + store per-cell selected question difficulty label
    // Key format: "playerNum:row:col"
    private final Map<String, String> pendingQuestionDiffByCell = new ConcurrentHashMap<>();

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

        // ✅ FIX: prevent window from shrinking and hiding a board
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(700);

        mpDispatcher = new MultiplayerDispatcher(this, MultiplayerDispatcher.Role.OFFLINE, null);

        init();
        setupEventHandlers();
        startTimer();
    }

    @Override
    public void onGameModelChanged() {
        Platform.runLater(this::updateUI);
    }

    public void attachMultiplayer(MultiplayerSession session) {
        this.multiplayerSession = session;
        this.multiplayerEnabled = true;
        this.localPlayerNum = session.isHost() ? 1 : 2;

        // ✅ Enable auto-fit scaling ONLY in multiplayer (keeps UI same, just scales if needed)
        Platform.runLater(() -> gameView.enableMultiplayerAutoFit(N, M));

        // ✅ Receive seed + difficulty + names from host
        session.setOnGameSettings(settings -> Platform.runLater(() -> {

            // seed for next NEW_GAME
            pendingNewGameSeed = settings.seed;

            // difficulty must be identical on both sides
            this.difficulty = settings.difficulty;

            // ✅ restore player names on receiver
            if (settings.hostName != null && !settings.hostName.isBlank()) {
                this.player1Name = settings.hostName;
            }
            if (settings.joinName != null && !settings.joinName.isBlank()) {
                this.player2Name = settings.joinName;
            }

            updateUI(); // refresh labels immediately

            // ✅ re-apply auto-fit after settings (safe)
            gameView.enableMultiplayerAutoFit(N, M);
        }));

        session.setOnActionReceived(action -> Platform.runLater(() -> applyRemoteAction(action)));

        // ✅ receive QUESTION_RESULT (no popup on remote)
        session.setOnQuestionResult(payload -> Platform.runLater(() -> applyQuestionResultFromNetwork(payload)));

        // Game Over broadcast
        session.setOnGameOver(payload -> Platform.runLater(() -> endGameFromNetwork(payload)));

        // ✅ ONLY New Game uses approval now
        session.setOnActionRequest(action -> Platform.runLater(() -> {
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

        // ✅ Execute actions coming from the OTHER side
        session.setOnExecuteAction(action -> Platform.runLater(() -> {

            if (action.type == MenuAction.Type.NEW_GAME) {

                long seedToUse;

                // ✅ Host chooses ONE seed and broadcasts it, then both run the same init()
                if (multiplayerEnabled && multiplayerSession != null && multiplayerSession.isHost()) {

                    seedToUse = System.nanoTime();

                    // store locally too (host uses same seed)
                    pendingNewGameSeed = seedToUse;

                    // send seed + difficulty + names to client BEFORE starting
                    try {
                        multiplayerSession.sendGameSettings(
                                new GameSettings(player1Name, player2Name,difficulty, seedToUse)
                        );
                    } catch (Exception ignored) {}

                } else {
                    // ✅ Client must use the seed that arrived from host (DO NOT fallback)
                    if (pendingNewGameSeed == null) {
                        System.err.println("[MP] NEW_GAME executed but no seed received yet!");
                        return;
                    }
                    seedToUse = pendingNewGameSeed;
                }

                pendingNewGameSeed = null;

                // ✅ Start new game using the SAME seed on both sides
                executeNewGameNowWithSeed(seedToUse);

                // ✅ re-apply auto-fit after rebuilding boards
                Platform.runLater(() -> gameView.enableMultiplayerAutoFit(N, M));
                return;
            }

            if (action.type == MenuAction.Type.RETURN_MENU) {
                // close session + go menu (this is for the RECEIVER side)
                disableMultiplayerLocally();

                if (gameTimer != null) gameTimer.cancel();
                Main.showMainMenu(primaryStage);
            }
        }));

        session.setOnActionDeclined(type -> Platform.runLater(() -> {
            showMessage("Request Declined", "The other player declined the New Game request.");
        }));

        // ✅ Only show message; DO NOT close session here
        session.setOnPlayerLeft(name -> Platform.runLater(() -> {
            showMessage("Player Left", "Player " + name + " is out now.");
        }));
    }


    private void disableMultiplayerLocally() {
        multiplayerEnabled = false;
        // close session safely
        if (multiplayerSession != null) {
            try { multiplayerSession.close(); } catch (Exception ignored) {}
        }
        multiplayerSession = null;
        localPlayerNum = -1;

        // ✅ clear pending map
        pendingQuestionDiffByCell.clear();
    }
    
    private void resetSeed(long newSeed) {
        this.seed = newSeed;
        this.rng = new Random(newSeed);
        this.specialCellService = new SpecialCellService(this.rng);

        // recreate model so board generation uses the NEW rng
        this.gameModel = new GameModel(this, mineCount, sharedLives, rng);
        this.gameModel.addObserver(this);
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
    	
    	SoundService.initGameSounds(); 
        gameActive = true;
        endGameTriggered = false;
        endGameDialogShown = false;
        currentPlayer = 1;
        elapsedTime = 0;

        // ✅ clear pending question contexts on reset
        pendingQuestionDiffByCell.clear();

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

        // ✅ Exit -> confirm first, then exit. (Optional: also return other to menu)
        gameView.exitBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {

            boolean ok = showConfirmation("Exit Game", "Are you sure you want to exit the game?", "Exit");
            if (!ok) return;

            if (multiplayerEnabled && multiplayerSession != null) {
                String me = multiplayerSession.isHost() ? player1Name : player2Name;

                // Best effort notify + force other to menu
                try { multiplayerSession.notifyPlayerLeft(me); } catch (Exception ignored) {}

                try {
                    multiplayerSession.sendMessage(new MpMessage(
                            MpMessageType.EXECUTE_ACTION,
                            new MenuAction(MenuAction.Type.RETURN_MENU, me)
                    ));
                } catch (Exception ignored) {}

                // Close local session
                disableMultiplayerLocally();
            }

            Platform.exit();
            System.exit(0);
        });

        // ✅ Return to Menu -> confirm first, then notify+force other, then return locally
        gameView.backToMenuBtn.setOnAction(e -> {

            boolean ok = showConfirmation("Return to Menu",
                    "Return to the main menu?\nCurrent game progress will be lost.",
                    "Return");

            if (!ok) return;

            if (multiplayerEnabled && multiplayerSession != null) {
                String me = multiplayerSession.isHost() ? player1Name : player2Name;

                // 1) show message on other side
                try { multiplayerSession.notifyPlayerLeft(me); } catch (Exception ignored) {}

                // 2) force OTHER side to menu
                try {
                    multiplayerSession.sendMessage(new MpMessage(
                            MpMessageType.EXECUTE_ACTION,
                            new MenuAction(MenuAction.Type.RETURN_MENU, me)
                    ));
                } catch (Exception ignored) {}

                // ✅ 3) execute locally immediately (you will NOT receive your own message)
                disableMultiplayerLocally();
                if (gameTimer != null) gameTimer.cancel();
                Main.showMainMenu(primaryStage);
                return;
            }

            // Offline
            if (gameTimer != null) gameTimer.cancel();
            Main.showMainMenu(primaryStage);
        });
    }

    private void executeNewGameNowWithSeed(long newSeed) {
        if (gameTimer != null) gameTimer.cancel();
        if (!multiplayerEnabled) {
            SysData.deleteLocalResumePayload();
        }
        resetSeed(newSeed);
        init();
        startTimer();
    }

    private void executeNewGameNow() {
        // ✅ offline stays random
        executeNewGameNowWithSeed(System.nanoTime());
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
    
    // ===================== Local (Offline) Resume Support =====================
    // IMPORTANT: OFFLINE ONLY. Must NEVER run in multiplayer.

    public void maybeAutoSaveLocal() {
        if (multiplayerEnabled) return;
        if (!isGameActive()) return;

        try {
            LocalGameState st = exportLocalState();
            String payload = LocalResumeCodec.encode(st);
            SysData.saveLocalResumePayload(payload);
        } catch (Exception ignored) {
            // Never crash gameplay due to save issues
        }
    }

    public LocalGameState exportLocalState() {
        if (multiplayerEnabled) {
            throw new IllegalStateException("exportLocalState must not be called in multiplayer mode");
        }

        LocalGameState st = new LocalGameState();
        st.difficulty = this.difficulty;
        st.p1Name = this.player1Name;
        st.p2Name = this.player2Name;

        st.seed = this.seed;
        st.currentPlayer = this.currentPlayer;
        st.sharedScore = this.gameModel.getSharedScore();
        st.sharedLives = this.gameModel.getSharedLives();
        st.elapsedTimeSec = this.elapsedTime;

        st.board1 = LocalGameState.captureBoard(this.gameModel.getBoard1());
        st.board2 = LocalGameState.captureBoard(this.gameModel.getBoard2());

        return st;
    }

    public void applyLocalResumePayload(String payload) {
        if (multiplayerEnabled) return;

        Optional<LocalGameState> opt = LocalResumeCodec.decode(payload);
        if (!opt.isPresent()) return;

        LocalGameState st = opt.get();

        // Restore primary fields
        this.currentPlayer = (st.currentPlayer == 2) ? 2 : 1;
        this.elapsedTime = Math.max(0, st.elapsedTimeSec);

        this.gameModel.setSharedScore(st.sharedScore);
        this.gameModel.setSharedLives(Math.max(0, st.sharedLives));

        // Apply board flags/open/discovered/activated/flagScored
        LocalGameState.applyBoard(this.gameModel.getBoard1(), st.board1);
        LocalGameState.applyBoard(this.gameModel.getBoard2(), st.board2);

        // Refresh UI cells
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < M; c++) {
                board1[r][c].init();
                board2[r][c].init();
            }
        }

        // Fix counters (mines left, etc.)
        if (board1Controller != null) board1Controller.recalculateCountersFromCells();
        if (board2Controller != null) board2Controller.recalculateCountersFromCells();

        highlightCurrentPlayer();
        updateUI();
        checkWinCondition();
    }

    public long getSeed() {
        return seed;
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
        
        //  surprise activation SFX
        if (res.newScore > gameModel.getSharedScore()
            || res.newLives > gameModel.getSharedLives()) {
            SoundService.playGoodSurprise();
        } else {
            SoundService.playBadSurprise();
        }

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
        
        maybeAutoSaveLocal();

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
            // question result SFX
            if (correct) {
                SoundService.playCorrectAnswer();
            } else {
                SoundService.playWrongAnswer();
            }


            applyQuestionReward(correct, qDiffLabel);

            Cell cell = cellCtrl.getCell();
            cell.setActivated(true);
            cellCtrl.init();

            if (gameModel.getSharedLives() <= 0) {
                endGame(false);
            }
            else {
            // ✅ Save immediately after applying question consequences.
            // Multiplayer is unaffected (guarded in maybeAutoSaveLocal()).
            maybeAutoSaveLocal();
            }
        }
    }

    // ✅ NEW: multiplayer-safe entry point called from BoardController
    // - Offline: same as activateQuestionCell(...)
    // - Multiplayer:
    //   * Local player => show popup, apply reward, send QUESTION_RESULT
    //   * Remote click => silently select question (to keep RNG in sync), store qDiffLabel, wait for QUESTION_RESULT
    public void handleQuestionCellTriggered(int boardPlayerNum, int row, int col, CellController cellCtrl) {
        if (!isGameActive()) return;

        // Offline behavior unchanged
        if (!multiplayerEnabled || multiplayerSession == null) {
            activateQuestionCell(cellCtrl);
            return;
        }

        // Load questions (same as your original)
        List<Question> all = SysData.loadQuestions();
        if (all == null || all.isEmpty()) {
            // Only local side shows message (avoid remote spam)
            if (boardPlayerNum == localPlayerNum) {
                showMessage("No Questions", "No questions found in QuestionsCSV.csv");
            }
            return;
        }

        final String key = makeQuestionKey(boardPlayerNum, row, col);

        // Remote action on this PC: consume RNG in the SAME way but DO NOT show popup
        if (boardPlayerNum != localPlayerNum) {
            // If already prepared, don't consume RNG twice
            if (!pendingQuestionDiffByCell.containsKey(key)) {
                Question q = all.get(rng.nextInt(all.size()));
                String qDiffLabel = mapDifficultyLabel(q.getDifficulty());
                pendingQuestionDiffByCell.put(key, qDiffLabel);
            }
            return;
        }

        // Local click: consume RNG (select question), show popup, apply reward, send result
        Question q = all.get(rng.nextInt(all.size()));
        String qDiffLabel = mapDifficultyLabel(q.getDifficulty());

        Optional<Boolean> result = QuestionPopup.show(primaryStage, q, qDiffLabel);

        if (!isGameActive()) return;

        if (result.isPresent()) {
            boolean correct = result.get();

            // apply locally (with dialogs)
            applyQuestionReward(correct, qDiffLabel);

            Cell cell = cellCtrl.getCell();
            cell.setActivated(true);
            cellCtrl.init();

            // send to other side (so remote applies without popup)
            try {
                multiplayerSession.sendQuestionResult(
                        new QuestionResultPayload(boardPlayerNum, row, col, correct, qDiffLabel)
                );
            } catch (Exception ignored) {}

            if (gameModel.getSharedLives() <= 0) {
                endGame(false);
            }
        }
    }

    // ✅ NEW: apply QUESTION_RESULT from network (no popup)
    private void applyQuestionResultFromNetwork(QuestionResultPayload p) {
        if (p == null) return;
        if (!multiplayerEnabled || multiplayerSession == null) return;

        // Find the cell controller on the correct board
        CellController[][] board = (p.playerNum == 1) ? board1 : board2;
        if (board == null) return;

        if (p.row < 0 || p.col < 0 || p.row >= board.length || p.col >= board[0].length) return;

        CellController cellCtrl = board[p.row][p.col];
        if (cellCtrl == null) return;

        Cell cell = cellCtrl.getCell();
        if (cell == null) return;

        // Avoid double-apply
        if (cell.isActivated()) return;

        // Prefer the locally-prepared qDiffLabel (keeps RNG alignment), fallback to payload
        String key = makeQuestionKey(p.playerNum, p.row, p.col);
        String qDiffLabel = pendingQuestionDiffByCell.remove(key);
        if (qDiffLabel == null || qDiffLabel.isBlank()) {
            qDiffLabel = (p.qDiffLabel != null ? p.qDiffLabel : "Easy");
        }

        // Apply reward WITHOUT dialogs on remote (no popup + no message spam)
        applyQuestionRewardInternal(p.correct, qDiffLabel, false);

        // mark activated and refresh UI
        cell.setActivated(true);
        cellCtrl.init();

        updateUI();
        checkWinCondition();

        if (gameModel.getSharedLives() <= 0) {
            endGame(false);
        }
    }

    private String makeQuestionKey(int playerNum, int row, int col) {
        return playerNum + ":" + row + ":" + col;
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

    // ✅ keep your original public method exactly, but route to internal with dialogs=true
    private void applyQuestionReward(boolean correct, String qDiff) {
        applyQuestionRewardInternal(correct, qDiff, true);
    }

    // ✅ NEW internal method so remote can apply silently (no popups/messages)
    private void applyQuestionRewardInternal(boolean correct, String qDiff, boolean showDialogs) {

        if (showDialogs) {
            if (correct) gameView.onQuestionCorrect();
            else         gameView.onQuestionWrong();
        }

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
            if (showDialogs) {
                if (done) extraInfo.append("\nMine gift: one hidden mine has been marked on your board.");
                else      extraInfo.append("\nMine gift: no hidden mines left to mark.");
            }
        }

        if (res.actions.contains(SpecialAction.REVEAL_AREA_3X3)) {
            int revealed = revealBestAvailableBlockForCurrentPlayer();
            if (showDialogs) {
                extraInfo.append("\nReveal bonus: ")
                        .append(revealed)
                        .append(" cells have been uncovered.");
            }
        }

        if (showDialogs) {
            String msg = res.message;
            if (extraInfo.length() > 0) msg += "\n" + extraInfo;
            showMessage(res.title, msg);
        }

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
        
        if (!multiplayerEnabled) {
            SysData.deleteLocalResumePayload();
        }


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

        menuBtn.setOnAction(e -> {
            dialog.close();

            if (multiplayerEnabled && multiplayerSession != null) {
                String me = multiplayerSession.isHost() ? player1Name : player2Name;

                // 1) message on other side
                try { multiplayerSession.notifyPlayerLeft(me); } catch (Exception ignored) {}

                // 2) force OTHER side to menu
                try {
                    multiplayerSession.sendMessage(new MpMessage(
                            MpMessageType.EXECUTE_ACTION,
                            new MenuAction(MenuAction.Type.RETURN_MENU, me)
                    ));
                } catch (Exception ignored) {}

                // ✅ 3) execute locally immediately
                disableMultiplayerLocally();
                if (gameTimer != null) gameTimer.cancel();
                Main.showMainMenu(primaryStage);
                return;
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

    private void returnToMenuNow() {
        if (gameTimer != null) gameTimer.cancel();
        Main.showMainMenu(primaryStage);
    }
}
