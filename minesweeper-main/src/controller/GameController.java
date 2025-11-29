package controller;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import model.Board;
import model.Cell;
import model.GameModel;
import model.Question;
import model.QuestionDifficulty;
import model.QuestionRepository;
import view.GameView;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main game controller for cooperative two-player Minesweeper
 * Manages turn-based gameplay with shared score and lives.
 *
 * Now uses:
 *  - GameModel → logical state (Board + Cell objects)
 *  - CellController[][] → UI wrappers around each Cell
 */
public class GameController {
    public int N, M;
    public GameView gameView;
    public GameModel gameModel;

    // UI wrappers for each cell (one array per player board)
    public CellController[][] board1;
    public CellController[][] board2;

    // Player names
    public String player1Name = "Player 1";
    public String player2Name = "Player 2";

    // Game state
    private int currentPlayer = 1; // 1 or 2
    private boolean gameActive = true;

    // Timer
    private Timer gameTimer;
    private int elapsedTime = 0;

    // Difficulty settings
    private String difficulty;
    private int mineCount;
    private int sharedLives;

    public GameController(String difficulty, String p1Name, String p2Name) {
        this.difficulty = difficulty;
        this.player1Name = p1Name;
        this.player2Name = p2Name;

        // Set board size and parameters based on difficulty
        switch (difficulty) {
            case "Easy":
                N = M = 9;
                mineCount = 10;
                sharedLives = 10;
                break;
            case "Medium":
                N = M = 13;
                mineCount = 26;
                sharedLives = 8;
                break;
            case "Hard":
                N = M = 16;
                mineCount = 44;
                sharedLives = 6;
                break;
            default:
                N = M = 9;
                mineCount = 10;
                sharedLives = 10;
        }

        // Create model and view
        gameModel = new GameModel(this, mineCount, sharedLives);
        gameView = new GameView(this);

        init();
        setupEventHandlers();
        startTimer();
    }

    public void init() {
        gameActive = true;
        currentPlayer = 1;
        elapsedTime = 0;

        // Initialize logical boards in the model
        gameModel.initializeBoards(N, M);
        Board logicalBoard1 = gameModel.getBoard1();
        Board logicalBoard2 = gameModel.getBoard2();

        // Create UI wrappers (CellController) for each logical Cell
        board1 = createUiBoard(logicalBoard1);
        board2 = createUiBoard(logicalBoard2);

        // Place the CellViews in the GridPanes
        createCellsGrid(board1, gameView.gridPane1);
        createCellsGrid(board2, gameView.gridPane2);

        // Add mouse event handlers
        addEventHandlerToBoard(board1, 1);
        addEventHandlerToBoard(board2, 2);

        updateUI();
        highlightCurrentPlayer();
    }

    /** Build a 2D array of CellController from a logical Board. */
    private CellController[][] createUiBoard(Board logicalBoard) {
        int rows = logicalBoard.getRows();
        int cols = logicalBoard.getCols();
        CellController[][] uiBoard = new CellController[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = logicalBoard.getCell(i, j);
                uiBoard[i][j] = new CellController(cell);
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

    private void setupEventHandlers() {
        gameView.restartBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (gameTimer != null) gameTimer.cancel();
            init();
            startTimer();
        });

        gameView.exitBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            Platform.exit();
            System.exit(0);
        });

        gameView.endTurnBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (gameActive) {
                switchPlayer();
            }
        });
    }

    private void addEventHandlerToBoard(CellController[][] board, int playerNum) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                int row = i;
                int col = j;
                board[i][j].cellView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                        new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                // Only allow actions if it's this player's turn and game is active
                                if (!gameActive || currentPlayer != playerNum) return;

                                if (event.getButton() == MouseButton.PRIMARY) {
                                    handleLeftClick(board, row, col, playerNum);
                                } else if (event.getButton() == MouseButton.SECONDARY) {
                                    handleRightClick(board, row, col);
                                }
                            }
                        }
                );
            }
        }
    }

    private void handleLeftClick(CellController[][] board, int row, int col, int playerNum) {
        CellController cellCtrl = board[row][col];
        Cell cell = cellCtrl.getCell();

        // 1) If it's a discovered special cell and not yet activated -> activate it
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

        // 2) If cell is already open or flagged (and not a "click to activate" special), ignore
        if (cell.isOpen() || cell.isFlag()) return;

        // 3) Open the cell (root click)
        openCell(board, row, col, true);

        // 4) Check cell type and handle accordingly
        if (cell.isMine()) {
            handleMineHit();
            switchPlayer();
        } else if (cell.isSurprise()) {
            cell.setDiscovered(true);
            cellCtrl.init();
            showMessage("Surprise Cell Discovered!",
                    "A Surprise Cell has been found!\nYou can activate it on your next turn.");
            switchPlayer(); // ends turn (SRS 3.2.18)
        } else if (cell.isQuestion()) {
            cell.setDiscovered(true);
            cellCtrl.init();
            showMessage("Question Cell Discovered!",
                    "A Question Cell has been found!\nYou can activate it on your next turn.");
            switchPlayer(); // ends turn (SRS 3.2.18)
        } else {
            // Normal number/empty cell: revealing ends the turn (SRS 3.2.8)
            switchPlayer();
        }

        updateUI();
        checkWinCondition();
    }

    private void handleRightClick(CellController[][] board, int row, int col) {
        CellController cellCtrl = board[row][col];
        Cell cell = cellCtrl.getCell();

        // Can only flag unopened cells
        if (cell.isOpen()) return;

        boolean wasFlagged = cell.isFlag();
        cell.toggleFlag();
        boolean isFlagged = cell.isFlag();

        // Apply flagging rewards/penalties ONCE per cell (SRS 3.2.10–3.2.13)
        if (!wasFlagged && isFlagged && !cell.isFlagScored()) {
            if (cell.isMine()) {
                gameModel.sharedScore += 1;   // +1 for correct flag
            } else {
                gameModel.sharedScore -= 3;   // -3 for incorrect flag
            }
            cell.setFlagScored(true); // prevent double scoring
        }

        cellCtrl.init();
        updateUI();
        // Flagging doesn't end turn (SRS 3.2.9)
    }

    // Simple version kept for compatibility if you ever call it elsewhere
    private void openCell(CellController[][] board, int row, int col) {
        openCell(board, row, col, true);
    }

    /**
     * Open a cell, with flood-fill behavior that:
     * - Treats Surprise and Question cells like "empty" for expansion
     * - Never expands through mines
     * - Gives +1 point only for the root clicked non-special cell
     */
    private void openCell(CellController[][] board, int row, int col, boolean isRootClick) {
        if (!isInBoard(row, col)) return;

        CellController cellCtrl = board[row][col];
        Cell cell = cellCtrl.getCell();

        // Already open or flagged? do nothing
        if (cell.isOpen() || cell.isFlag()) return;

        // Open it
        cell.setOpen(true);
        gameModel.revealedCells++;

        boolean isMine = cell.isMine();
        boolean isSpecial = cell.isSpecial();
        int neighbors = cell.getNeighborMinesNum();

        // If a Surprise / Question cell is revealed (even via cascade),
        // mark it as discovered so a later click can activate it.
        if (isSpecial && !cell.isDiscovered()) {
            cell.setDiscovered(true);
        }

        // Scoring: +1 point for the root clicked non-special cell only
        if (isRootClick && !isMine && !isSpecial) {
            gameModel.sharedScore += 1;
        }

        // Decide if we should expand from this cell:
        // - Never from mines
        // - Expand from empty cells (0 neighbors)
        // - ALSO expand from Surprise / Question cells (treat them like empty)
        boolean shouldExpand =
                !isMine && (neighbors == 0 || isSpecial);

        if (shouldExpand) {
            for (int i = row - 1; i <= row + 1; i++) {
                for (int j = col - 1; j <= col + 1; j++) {
                    if (isInBoard(i, j) && !(i == row && j == col)) {
                        openCell(board, i, j, false); // cascade: no extra scoring
                    }
                }
            }
        }

        // Refresh cell view
        cellCtrl.init();
    }

    private void handleMineHit() {
        gameModel.sharedLives--; // SRS 3.2.15
        showMessage("Mine Hit!",
                "You hit a mine! -1 life\n\nShared Lives Remaining: " + gameModel.sharedLives);

        if (gameModel.sharedLives <= 0) {
            endGame(false);
        }
    }

    private void activateSurpriseCell(CellController cellCtrl) {
        Cell cell = cellCtrl.getCell();

        // Calculate cost based on difficulty (SRS 3.2.22)
        int cost = difficulty.equals("Easy") ? 5 : difficulty.equals("Medium") ? 8 : 12;

        if (gameModel.sharedScore < cost) {
            showMessage("Insufficient Score",
                    "You need " + cost + " points to activate this Surprise Cell.\nCurrent score: " + gameModel.sharedScore);
            return;
        }

        gameModel.sharedScore -= cost;

        // 50% chance good or bad (SRS 3.2.23)
        boolean isGood = new Random().nextBoolean();

        // Bonus/penalty points based on difficulty (SRS 3.2.24, 3.2.25)
        int bonusPoints = difficulty.equals("Easy") ? 8 : difficulty.equals("Medium") ? 12 : 16;

        if (isGood) {
            // Good outcome (SRS 3.2.24)
            gameModel.sharedLives++;
            gameModel.sharedScore += bonusPoints;

            // Check life limit (SRS 3.2.27, 3.2.28)
            if (gameModel.sharedLives > 10) {
                int extraLives = gameModel.sharedLives - 10;
                gameModel.sharedLives = 10;
                int convertedPoints = extraLives * (difficulty.equals("Easy") ? 5 : difficulty.equals("Medium") ? 8 : 12);
                gameModel.sharedScore += convertedPoints;
                showMessage("Good Surprise! ✓",
                        "✓ +1 life (max reached, converted extra to +" + convertedPoints + " pts)\n" +
                                "✓ +" + bonusPoints + " bonus points!\n\n" +
                                "Lives: " + gameModel.sharedLives + " | Score: " + gameModel.sharedScore);
            } else {
                showMessage("Good Surprise! ✓",
                        "✓ +1 life\n✓ +" + bonusPoints + " bonus points!\n\n" +
                                "Lives: " + gameModel.sharedLives + " | Score: " + gameModel.sharedScore);
            }
        } else {
            // Bad outcome (SRS 3.2.25)
            gameModel.sharedLives--;
            gameModel.sharedScore -= bonusPoints;
            showMessage("Bad Surprise! ✗",
                    "✗ -1 life\n✗ -" + bonusPoints + " points\n\n" +
                            "Lives: " + gameModel.sharedLives + " | Score: " + gameModel.sharedScore);

            if (gameModel.sharedLives <= 0) {
                endGame(false);
                return;
            }
        }

        cell.setActivated(true);
        cellCtrl.init();
        updateUI();
    }

    private void activateQuestionCell(CellController cellCtrl) {
        // Show question dialog
        QuestionDialog dialog = new QuestionDialog(difficulty);
        Optional<Boolean> result = dialog.showAndWait();

        if (result.isPresent()) {
            boolean correct = result.get();
            String questionDifficulty = dialog.getQuestionDifficulty();

            applyQuestionReward(correct, questionDifficulty);
            Cell cell = cellCtrl.getCell();
            cell.setActivated(true);
            cellCtrl.init();
            updateUI();

            if (gameModel.sharedLives <= 0) {
                endGame(false);
            }
        }
    }

    private void applyQuestionReward(boolean correct, String qDiff) {
        // Apply rewards based on game difficulty and question difficulty
        // This follows the exact table from SRS 3.2.33
        int points = 0;
        int lives = 0;

        if (difficulty.equals("Easy")) {
            switch (qDiff) {
                case "Easy":
                    points = correct ? 3 : -3;
                    lives = correct ? 1 : 0;
                    break;
                case "Intermediate":
                    points = correct ? 6 : -6;
                    break;
                case "Hard":
                    points = correct ? 10 : -10;
                    break;
                case "Expert":
                    points = correct ? 15 : -15;
                    lives = correct ? 2 : -1;
                    break;
            }
        } else if (difficulty.equals("Medium")) {
            switch (qDiff) {
                case "Easy":
                    points = correct ? 8 : -8;
                    lives = correct ? 1 : 0;
                    break;
                case "Intermediate":
                    points = correct ? 10 : -10;
                    lives = correct ? 1 : 0;
                    break;
                case "Hard":
                    points = correct ? 15 : -15;
                    lives = correct ? 1 : -1;
                    break;
                case "Expert":
                    points = correct ? 20 : -20;
                    lives = correct ? 2 : -2;
                    break;
            }
        } else { // Hard
            switch (qDiff) {
                case "Easy":
                    points = correct ? 10 : -10;
                    lives = correct ? 1 : -1;
                    break;
                case "Intermediate":
                    points = correct ? 15 : -15;
                    lives = correct ? 2 : -2;
                    break;
                case "Hard":
                    points = correct ? 20 : -20;
                    lives = correct ? 2 : -2;
                    break;
                case "Expert":
                    points = correct ? 40 : -40;
                    lives = correct ? 3 : -3;
                    break;
            }
        }

        gameModel.sharedScore += points;
        gameModel.sharedLives += lives;

        // Apply life limit (SRS 3.2.27, 3.2.28)
        if (gameModel.sharedLives > 10) {
            int extraLives = gameModel.sharedLives - 10;
            gameModel.sharedLives = 10;
            int convertedPoints = extraLives * (difficulty.equals("Easy") ? 5 : difficulty.equals("Medium") ? 8 : 12);
            gameModel.sharedScore += convertedPoints;
        }

        String message = correct ?
                "Correct Answer! ✓\n+" + Math.abs(points) + " points" + (lives > 0 ? ", +" + lives + " lives" : "") :
                "Wrong Answer ✗\n" + points + " points" + (lives < 0 ? ", " + lives + " lives" : "");
        showMessage("Question Result", message + "\n\nScore: " + gameModel.sharedScore + " | Lives: " + gameModel.sharedLives);
    }

    private boolean isInBoard(int row, int col) {
        return row >= 0 && row < N && col >= 0 && col < M;
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        highlightCurrentPlayer();
        updateUI();
    }

    private void highlightCurrentPlayer() {
        String activeBorder =
                "-fx-border-color: #22C55E;" +          // bright green
                        "-fx-border-width: 3;" +
                        "-fx-padding: 18;" +
                        "-fx-background-color: #111827;" +      // dark card
                        "-fx-border-radius: 14;" +
                        "-fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0, 0, 6);";

        String inactiveBorder =
                "-fx-border-color: #374151;" +          // neutral gray
                        "-fx-border-width: 2;" +
                        "-fx-padding: 18;" +
                        "-fx-background-color: #111827;" +
                        "-fx-border-radius: 14;" +
                        "-fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 4);";

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
        gameView.currentPlayerLabel.setText((currentPlayer == 1 ? player1Name : player2Name) + "'s Turn");
        gameView.difficultyLabel.setText(difficulty);
        gameView.timeLabel.setText(formatTime(elapsedTime));

        // Color lives red if low
        if (gameModel.sharedLives <= 3) {
            gameView.sharedLivesLabel.setTextFill(Color.web("#FCA5A5")); // soft red
        } else {
            gameView.sharedLivesLabel.setTextFill(Color.web("#A5B4FC")); // bluish
        }
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private void checkWinCondition() {
        // Win condition (SRS 3.2.40): all non-mine cells revealed
        int totalCells = N * M * 2; // Two boards
        int totalMines = mineCount * 2;
        int safeCells = totalCells - totalMines;

        if (gameModel.revealedCells >= safeCells) {
            endGame(true);
        }
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

    private void endGame(boolean won) {
        gameActive = false;
        if (gameTimer != null) gameTimer.cancel();

        // Convert remaining lives to points (SRS 3.2.38)
        int lifeBonus = gameModel.sharedLives * (difficulty.equals("Easy") ? 5 : difficulty.equals("Medium") ? 8 : 12);
        int finalScore = gameModel.sharedScore + lifeBonus;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(won ? "🎉 Victory!" : "Game Over");
        alert.setHeaderText(won ?
                "Congratulations " + player1Name + " and " + player2Name + "!" :
                "Out of Lives!");
        alert.setContentText(
                "Difficulty: " + difficulty + "\n" +
                        "Time: " + formatTime(elapsedTime) + "\n\n" +
                        "Base Score: " + gameModel.sharedScore + "\n" +
                        "Lives Bonus: +" + lifeBonus + " (" + gameModel.sharedLives + " × " +
                        (difficulty.equals("Easy") ? 5 : difficulty.equals("Medium") ? 8 : 12) + ")\n" +
                        "═══════════════\n" +
                        "Final Score: " + finalScore + "\n\n" +
                        "Cells Revealed: " + gameModel.revealedCells + "/" + (N * M * 2 - mineCount * 2)
        );

        ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("New Game");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                init();
                startTimer();
            }
        });
    }

    private void showMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

/**
 * Question Dialog for Question Cells
 * Uses real questions from CSV via QuestionRepository.
 */
class QuestionDialog {

    private final Dialog<Boolean> dialog;
    private String questionDifficulty;   // "Easy", "Intermediate", "Hard", "Expert"
    private int correctIndex;           // field used by the buttons

    public QuestionDialog(String gameDifficulty) {
        dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Question Cell");

        // Load all questions from CSV
        List<Question> allQuestions = QuestionRepository.loadQuestions();
        Question chosen = null;

        if (allQuestions != null && !allQuestions.isEmpty()) {
            chosen = allQuestions.get(new Random().nextInt(allQuestions.size()));
            QuestionDifficulty diffEnum = chosen.getDifficulty();
            questionDifficulty = mapDifficultyLabel(diffEnum);   // for scoring + header
            dialog.setHeaderText(questionDifficulty + " Question");
        } else {
            // Fallback: no CSV questions → use generic difficulty
            String[] difficulties = {"Easy", "Intermediate", "Hard", "Expert"};
            questionDifficulty = difficulties[new Random().nextInt(difficulties.length)];
            dialog.setHeaderText(questionDifficulty + " Question");
        }

        // Build UI
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        String questionText;
        String[] answers;

        if (chosen != null) {
            questionText = chosen.getText();
            answers = chosen.getOptions();
            correctIndex = chosen.getCorrectIndex();   // set FIELD

            // Safety: ensure we have 4 options
            if (answers == null || answers.length < 4) {
                answers = new String[]{"Option A", "Option B", "Option C", "Option D"};
                correctIndex = 0;                      // set FIELD
            }
        } else {
            // Fallback sample
            questionText = "Sample question (no questions found in CSV).";
            answers = new String[]{"A", "B", "C", "D"};
            correctIndex = 0;                          // set FIELD
        }

        Label questionLabel = new Label(questionText);
        questionLabel.setWrapText(true);
        questionLabel.setFont(javafx.scene.text.Font.font("Arial",
                javafx.scene.text.FontWeight.BOLD, 14));
        questionLabel.setMaxWidth(400);
        content.getChildren().add(questionLabel);

        content.getChildren().add(new Label("")); // spacer

        Button[] buttons = new Button[4];
        for (int i = 0; i < 4; i++) {
            final int index = i;
            String answerText = (answers[i] == null || answers[i].isEmpty())
                    ? "(empty answer)"
                    : answers[i];

            buttons[i] = new Button((char) ('A' + i) + ")  " + answerText);
            buttons[i].setPrefWidth(400);
            buttons[i].setPrefHeight(45);
            buttons[i].setFont(javafx.scene.text.Font.font("Arial", 13));
            buttons[i].setStyle(
                    "-fx-background-color: #2196F3;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 6;" +
                            "-fx-cursor: hand;"
            );

            buttons[i].setOnMouseEntered(e ->
                    buttons[index].setStyle(
                            "-fx-background-color: #1976D2;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-background-radius: 6;" +
                                    "-fx-cursor: hand;" +
                                    "-fx-font-size: 13px;"
                    )
            );
            buttons[i].setOnMouseExited(e ->
                    buttons[index].setStyle(
                            "-fx-background-color: #2196F3;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-background-radius: 6;" +
                                    "-fx-cursor: hand;" +
                                    "-fx-font-size: 13px;"
                    )
            );

            // When user clicks an answer → set result and close dialog
            buttons[i].setOnAction(e -> {
                dialog.setResult(index == correctIndex);  // uses FIELD
                dialog.close();
            });

            content.getChildren().add(buttons[i]);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().clear(); // no default OK/Cancel
        dialog.getDialogPane().setPrefSize(480, 400);
    }

    /**
     * Map enum difficulty to the string used by applyQuestionReward.
     * EASY -> "Easy"
     * MEDIUM -> "Intermediate"
     * HARD -> "Hard"
     * EXPERT -> "Expert"
     */
    private String mapDifficultyLabel(QuestionDifficulty diff) {
        if (diff == null) return "Easy";

        switch (diff) {
            case EASY:
                return "Easy";
            case MEDIUM:
                return "Intermediate";  // matches applyQuestionReward's "Intermediate"
            case HARD:
                return "Hard";
            case EXPERT:
                return "Expert";
            default:
                return "Easy";
        }
    }

    public String getQuestionDifficulty() {
        return questionDifficulty;
    }

    public Optional<Boolean> showAndWait() {
        return dialog.showAndWait();
    }
}
