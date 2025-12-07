package controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Board;
import model.Cell;
import model.GameHistoryEntry;
import model.GameModel;
import model.Question;
import model.QuestionDifficulty;
import model.SysData;
import view.GameView;

/**
 * Main game controller for cooperative two-player Minesweeper.
 * Uses:
 *   - Board / Cell (model)
 *   - CellController + GameView (view)
 */
public class GameController {

    // Board size
    public int N, M;

    // MVC parts
    public GameView gameView;
    public GameModel gameModel;

    // Board UI (wrappers around Cell)
    public CellController[][] board1;
    public CellController[][] board2;

    // Players
    public String player1Name = "Player 1";
    public String player2Name = "Player 2";

    // Game state
    private int currentPlayer = 1;  // 1 or 2
    private boolean gameActive = true;

    // Timer
    private Timer gameTimer;
    private int elapsedTime = 0;

    // Difficulty
    private String difficulty;
    private int mineCount;
    private int sharedLives;   // initial lives based on difficulty (3.2.36)

    // Stage reference to return to menu
    private final Stage primaryStage;

    // -------------------------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    public GameController(String difficulty, String p1Name, String p2Name, Stage stage) {
        this.primaryStage = stage;
        this.difficulty = difficulty;
        this.player1Name = p1Name;
        this.player2Name = p2Name;

        // Difficulty rules  (3.2.36)
        int cellSize;   // dynamic cell size per difficulty

        switch (difficulty) {
            case "Easy":
                N = M = 9;
                mineCount = 10;
                sharedLives = 10;   // Easy: 10 lives
                cellSize = 26;
                break;

            case "Medium":
                N = M = 13;
                mineCount = 26;
                sharedLives = 8;    // Medium: 8 lives
                cellSize = 24;
                break;

            case "Hard":
            default:
                N = M = 16;
                mineCount = 44;
                sharedLives = 6;    // Hard: 6 lives
                cellSize = 22;   // smallest so it fits nicely
                break;
        }

        // Tell CellController what cell size to use (global for this game)
        CellController.setCellSide(cellSize);

        // Create model & view
        gameModel = new GameModel(this, mineCount, sharedLives);
        gameView = new GameView(this);

        init();
        setupEventHandlers();
        startTimer();
    }

    // -------------------------------------------------------------------------
    // INIT
    // -------------------------------------------------------------------------
    public void init() {
        gameActive = true;
        currentPlayer = 1;
        elapsedTime = 0;

        // Reset boards in the model (creates new Board objects)
        gameModel.initializeBoards(N, M);

        Board logicalBoard1 = gameModel.getBoard1();
        Board logicalBoard2 = gameModel.getBoard2();

        // Build UI wrappers per logical Board
        board1 = createUiBoard(logicalBoard1);
        board2 = createUiBoard(logicalBoard2);

        // Populate the GridPane with CellViews
        createCellsGrid(board1, gameView.gridPane1);
        createCellsGrid(board2, gameView.gridPane2);

        // Add mouse interactions
        addEventHandlerToBoard(board1, 1);
        addEventHandlerToBoard(board2, 2);

        updateUI();
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
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                gridPane.add(board[i][j].cellView, i, j, 1, 1);
            }
        }
    }

    // -------------------------------------------------------------------------
    // EVENT HANDLERS FOR BUTTONS
    // -------------------------------------------------------------------------
    private void setupEventHandlers() {
        // Restart
        gameView.restartBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (gameTimer != null) gameTimer.cancel();
            init();
            startTimer();
        });

        // Exit
        gameView.exitBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            Platform.exit();
            System.exit(0);
        });

        // Return to Menu
        gameView.backToMenuBtn.setOnAction(e -> {
            if (gameTimer != null) gameTimer.cancel();
            Main.showMainMenu(primaryStage);
        });

        // No "End Turn" button – turns change automatically
    }

    // -------------------------------------------------------------------------
    // MOUSE HANDLERS FOR CELLS
    // -------------------------------------------------------------------------
    private void addEventHandlerToBoard(CellController[][] board, int playerNum) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                int row = i;
                int col = j;

                board[i][j].cellView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                        new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                if (!gameActive || currentPlayer != playerNum) return;

                                if (event.getButton() == MouseButton.PRIMARY) {
                                    handleLeftClick(board, row, col, playerNum);
                                } else if (event.getButton() == MouseButton.SECONDARY) {
                                    handleRightClick(board, row, col);
                                }
                            }
                        });
            }
        }
    }

    private void handleLeftClick(CellController[][] board, int row, int col, int playerNum) {
        CellController cellCtrl = board[row][col];
        Cell cell = cellCtrl.getCell();

        // Activate already-discovered specials
        if (cell.isSurprise() && cell.isDiscovered() && !cell.isActivated()) {
            activateSurpriseCell(cellCtrl);
            switchPlayer();
            return;
        }
        if (cell.isQuestion() && cell.isDiscovered() && !cell.isActivated()) {
            activateQuestionCell(cellCtrl);
            switchPlayer();
            return;
        }

        // Ignore if open or flagged
        if (cell.isOpen() || cell.isFlag()) return;

        // Open
        openCell(board, row, col, true);

        // Behaviour by type
        if (cell.isMine()) {
            handleMineHit();
            switchPlayer();
        } else if (cell.isSurprise()) {
            cell.setDiscovered(true);
            cellCtrl.init();
            showMessage("Surprise Cell Discovered!",
                    "You can activate it on your next turn.");
            switchPlayer();
        } else if (cell.isQuestion()) {
            cell.setDiscovered(true);
            cellCtrl.init();
            showMessage("Question Cell Discovered!",
                    "You can activate it on your next turn.");
            switchPlayer();
        } else {
            // Normal number / empty cell – turn ends
            switchPlayer();
        }

        updateUI();
        checkWinCondition();
    }

    private void handleRightClick(CellController[][] board, int row, int col) {
        CellController cellCtrl = board[row][col];
        Cell cell = cellCtrl.getCell();

        if (cell.isOpen()) return;   // no flagging open cells

        boolean wasFlagged = cell.isFlag();
        cell.toggleFlag();
        boolean isFlagged = cell.isFlag();

        if (!wasFlagged && isFlagged && !cell.isFlagScored()) {
            if (cell.isMine()) gameModel.sharedScore += 1;
            else               gameModel.sharedScore -= 3;
            cell.setFlagScored(true);
        }

        cellCtrl.init();
        updateUI();
    }

    // -------------------------------------------------------------------------
    // OPEN CELL + FLOOD-FILL
    // -------------------------------------------------------------------------
    private void openCell(CellController[][] board, int row, int col, boolean isRootClick) {
        if (!isInBoard(row, col)) return;

        CellController cellCtrl = board[row][col];
        Cell cell = cellCtrl.getCell();

        if (cell.isOpen() || cell.isFlag()) return;

        cell.setOpen(true);
        gameModel.revealedCells++;

        boolean isMine = cell.isMine();
        boolean isSpecial = cell.isSpecial();
        int neighbors = cell.getNeighborMinesNum();

        if (isSpecial && !cell.isDiscovered()) {
            cell.setDiscovered(true);
        }

        // 3.2.37 – update cumulative score after actions
        if (isRootClick && !isMine && !isSpecial) {
            gameModel.sharedScore += 1;
        }

        boolean shouldExpand =
                !isMine && (neighbors == 0 || isSpecial);

        if (shouldExpand) {
            for (int i = row - 1; i <= row + 1; i++) {
                for (int j = col - 1; j <= col + 1; j++) {
                    if (!(i == row && j == col)) {
                        openCell(board, i, j, false);
                    }
                }
            }
        }

        cellCtrl.init();
    }

    private boolean isInBoard(int row, int col) {
        return row >= 0 && row < N && col >= 0 && col < M;
    }

    // -------------------------------------------------------------------------
    // MINE HIT
    // -------------------------------------------------------------------------
    private void handleMineHit() {
        gameModel.sharedLives--;
        showMessage("Mine Hit!",
                "You hit a mine! -1 life.\nShared Lives Remaining: " + gameModel.sharedLives);
        if (gameModel.sharedLives <= 0) {
            endGame(false);
        }
    }

    // -------------------------------------------------------------------------
    // SURPRISE CELL ACTIVATION
    // -------------------------------------------------------------------------
    private void activateSurpriseCell(CellController cellCtrl) {
        Cell cell = cellCtrl.getCell();

        int cost = difficulty.equals("Easy") ? 5 :
                   difficulty.equals("Medium") ? 8 : 12;

        if (gameModel.sharedScore < cost) {
            showMessage("Insufficient Score",
                    "You need " + cost + " points to activate this Surprise Cell.\n" +
                    "Current score: " + gameModel.sharedScore);
            return;
        }

        gameModel.sharedScore -= cost;

        boolean isGood = new Random().nextBoolean();
        int bonusPoints = difficulty.equals("Easy") ? 8 :
                          difficulty.equals("Medium") ? 12 : 16;

        if (isGood) {
            gameModel.sharedLives++;
            gameModel.sharedScore += bonusPoints;

            if (gameModel.sharedLives > 10) {
                int extraLives = gameModel.sharedLives - 10;
                gameModel.sharedLives = 10;
                int convertedPoints = extraLives *
                        (difficulty.equals("Easy") ? 5 :
                         difficulty.equals("Medium") ? 8 : 12);
                gameModel.sharedScore += convertedPoints;

                showMessage("Good Surprise! ✓",
                        "✓ +1 life (max reached, extra lives converted to +" +
                        convertedPoints + " points)\n" +
                        "✓ +" + bonusPoints + " bonus points!");
            } else {
                showMessage("Good Surprise! ✓",
                        "✓ +1 life\n✓ +" + bonusPoints + " bonus points!");
            }
        } else {
            gameModel.sharedLives--;
            gameModel.sharedScore -= bonusPoints;

            showMessage("Bad Surprise! ✗",
                    "✗ -1 life\n✗ -" + bonusPoints + " points");

            if (gameModel.sharedLives <= 0) {
                endGame(false);
                return;
            }
        }

        cell.setActivated(true);
        cellCtrl.init();
        updateUI();
    }

    // -------------------------------------------------------------------------
    // QUESTION CELL ACTIVATION – INLINE DIALOG
    // -------------------------------------------------------------------------
    private void activateQuestionCell(CellController cellCtrl) {
        // Load questions from CSV
        List<Question> all = SysData.loadQuestions();
        if (all == null || all.isEmpty()) {
            showMessage("No Questions",
                    "No questions found in QuestionsCSV.csv");
            return;
        }

        // Pick random question
        Question q = all.get(new Random().nextInt(all.size()));

        // Map difficulty enum to label used by scoring table
        String qDiffLabel = mapDifficultyLabel(q.getDifficulty());

        // Build a small custom Dialog<Boolean>
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Question Cell");
        dialog.setHeaderText(qDiffLabel + " Question");

        VBox content = new VBox(10);
        content.setPadding(new Insets(16));

        Label qLabel = new Label(q.getText());
        qLabel.setWrapText(true);
        qLabel.setMaxWidth(420);

        content.getChildren().add(qLabel);

        String[] options = q.getOptions();
        if (options == null || options.length < 4) {
            options = new String[]{"A", "B", "C", "D"};
        }

        ButtonType[] btnTypes = new ButtonType[4];
        for (int i = 0; i < 4; i++) {
            char letter = (char) ('A' + i);
            btnTypes[i] = new ButtonType(letter + ") " + options[i], ButtonData.OTHER);
        }

        dialog.getDialogPane().getButtonTypes().addAll(btnTypes);
        dialog.getDialogPane().setContent(content);

        int correctIdx = q.getCorrectIndex();

        dialog.setResultConverter(btn -> {
            if (btn == null) return null;
            for (int i = 0; i < 4; i++) {
                if (btn == btnTypes[i]) {
                    return i == correctIdx;
                }
            }
            return null;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent()) {
            boolean correct = result.get();
            applyQuestionReward(correct, qDiffLabel);

            Cell cell = cellCtrl.getCell();
            cell.setActivated(true);
            cellCtrl.init();
            updateUI();

            if (gameModel.sharedLives <= 0) {
                endGame(false);
            }
        }
    }

    private String mapDifficultyLabel(QuestionDifficulty diff) {
        if (diff == null) return "Easy";
        switch (diff) {
            case EASY:   return "Easy";
            case MEDIUM: return "Intermediate";
            case HARD:   return "Hard";
            case EXPERT: return "Expert";
            default:     return "Easy";
        }
    }

    // -------------------------------------------------------------------------
    // QUESTION REWARD TABLE
    // -------------------------------------------------------------------------
    private void applyQuestionReward(boolean correct, String qDiff) {
        int points = 0;
        int lives = 0;

        if (difficulty.equals("Easy")) {
            switch (qDiff) {
                case "Easy":         points = correct ? 3  : -3;  lives = correct ? 1 : 0; break;
                case "Intermediate": points = correct ? 6  : -6;  break;
                case "Hard":         points = correct ? 10 : -10; break;
                case "Expert":       points = correct ? 15 : -15; lives = correct ? 2 : -1; break;
            }
        } else if (difficulty.equals("Medium")) {
            switch (qDiff) {
                case "Easy":         points = correct ? 8  : -8;  lives = correct ? 1 : 0; break;
                case "Intermediate": points = correct ? 10 : -10; lives = correct ? 1 : 0; break;
                case "Hard":         points = correct ? 15 : -15; lives = correct ? 1 : -1; break;
                case "Expert":       points = correct ? 20 : -20; lives = correct ? 2 : -2; break;
            }
        } else { // Hard
            switch (qDiff) {
                case "Easy":         points = correct ? 10 : -10; lives = correct ? 1 : -1; break;
                case "Intermediate": points = correct ? 15 : -15; lives = correct ? 2 : -2; break;
                case "Hard":         points = correct ? 20 : -20; lives = correct ? 2 : -2; break;
                case "Expert":       points = correct ? 40 : -40; lives = correct ? 3 : -3; break;
            }
        }

        gameModel.sharedScore += points;
        gameModel.sharedLives += lives;

        // Cap lives at 10, convert extras to points (same rule as Surprise)
        if (gameModel.sharedLives > 10) {
            int extraLives = gameModel.sharedLives - 10;
            gameModel.sharedLives = 10;
            int convertedPoints = extraLives *
                    (difficulty.equals("Easy") ? 5 :
                     difficulty.equals("Medium") ? 8 : 12);
            gameModel.sharedScore += convertedPoints;
        }

        String msg = correct ?
                "Correct ✓\n+" + Math.abs(points) + " points" +
                (lives > 0 ? ", +" + lives + " lives" : "") :
                "Wrong ✗\n" + points + " points" +
                (lives < 0 ? ", " + lives + " lives" : "");

        showMessage("Question Result",
                msg + "\n\nScore: " + gameModel.sharedScore +
                " | Lives: " + gameModel.sharedLives);
    }

    // -------------------------------------------------------------------------
    // PLAYER / UI STATE
    // -------------------------------------------------------------------------
    private void switchPlayer() {
        currentPlayer = (currentPlayer == 1 ? 2 : 1);
        highlightCurrentPlayer();
        updateUI();
    }

    private void highlightCurrentPlayer() {
        String activeBorder =
                "-fx-border-color: #22C55E;" +
                "-fx-border-width: 3;" +
                "-fx-padding: 18;" +
                "-fx-background-color: #111827;" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12,0,0,6);";

        String inactiveBorder =
                "-fx-border-color: #374151;" +
                "-fx-border-width: 2;" +
                "-fx-padding: 18;" +
                "-fx-background-color: #111827;" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8,0,0,4);";

        if (currentPlayer == 1) {
            gameView.player1Panel.setStyle(activeBorder);
            gameView.player2Panel.setStyle(inactiveBorder);
        } else {
            gameView.player1Panel.setStyle(inactiveBorder);
            gameView.player2Panel.setStyle(activeBorder);
        }
    }

    private void updateUI() {
        gameView.sharedScoreLabel.setText("" + gameModel.sharedScore);
        gameView.sharedLivesLabel.setText("" + gameModel.sharedLives);
        gameView.currentPlayerLabel.setText(
                (currentPlayer == 1 ? player1Name : player2Name) + "'s Turn");

        gameView.difficultyLabel.setText(difficulty);
        gameView.timeLabel.setText(formatTime(elapsedTime));

        if (gameModel.sharedLives <= 3) {
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

	// -------------------------------------------------------------------------
	// WIN CONDITION: game ends when ONE board is fully cleared
	// -------------------------------------------------------------------------
	private void checkWinCondition() {
		if (isBoardCleared(board1) || isBoardCleared(board2)) {
			endGame(true);
		}
	}
	
	/**
	 * Returns true if this board has all NON-mine cells opened.
	 * Used to end the game as soon as one player finishes their board.
	 */
	private boolean isBoardCleared(CellController[][] board) {
	    if (board == null) return false;

	    int safeCells = 0;
	    int openedSafeCells = 0;

	    for (int r = 0; r < N; r++) {
	        for (int c = 0; c < M; c++) {
	            Cell cell = board[r][c].getCell();

	            if (!cell.isMine()) {          // only care about safe cells
	                safeCells++;
	                if (cell.isOpen()) {
	                    openedSafeCells++;
	                }
	            }
	        }
	    }

	    return safeCells > 0 && openedSafeCells == safeCells;
	}


    private void startTimer() {
        if (gameTimer != null) gameTimer.cancel();

        gameTimer = new Timer();
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (gameActive) {
                        elapsedTime++;
                        updateUI();
                    }
                });
            }
        }, 0, 1000);
    }

    // -------------------------------------------------------------------------
    // END GAME + SAVE HISTORY
    // -------------------------------------------------------------------------
    private void endGame(boolean won) {
        gameActive = false;
        if (gameTimer != null) gameTimer.cancel();

        // 3.2.38 – convert remaining lives into score
        int lifeBonus = gameModel.sharedLives *
                (difficulty.equals("Easy") ? 5 :
                 difficulty.equals("Medium") ? 8 : 12);

        int finalScore = gameModel.sharedScore + lifeBonus;

        saveGameToHistory(won, finalScore);

        // 3.2.39 – display base score, life bonus and final score
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(won ? "🎉 Victory!" : "Game Over");
        alert.setHeaderText(
                won
                        ? "Well done " + player1Name + " & " + player2Name + "!"
                        : "Out of Lives!"
        );
        alert.setContentText(
                "Difficulty: " + difficulty + "\n" +
                "Time: " + formatTime(elapsedTime) + "\n\n" +
                "Base Score: " + gameModel.sharedScore + "\n" +
                "Lives Bonus: +" + lifeBonus + "\n" +
                "═══════ FINAL SCORE ═══════\n" +
                finalScore
        );

        ((Button) alert.getDialogPane().lookupButton(ButtonType.OK))
                .setText("New Game");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                init();
                startTimer();
            }
        });
    }

    private void saveGameToHistory(boolean won, int finalScore) {
        String dateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String result = won ? "WIN" : "LOSE";

        GameHistoryEntry entry = new GameHistoryEntry(
                dateTime,
                difficulty,
                player1Name,
                player2Name,
                result,
                finalScore,
                elapsedTime
        );

        SysData.saveGame(entry);
    }

    // -------------------------------------------------------------------------
    // SMALL HELPER
    // -------------------------------------------------------------------------
    private void showMessage(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
