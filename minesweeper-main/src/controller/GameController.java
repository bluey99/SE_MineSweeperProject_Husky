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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import model.Board;
import model.Cell;
import model.GameHistoryEntry;    // <-- your history model
import model.GameModel;
import model.Question;
import model.QuestionDifficulty;
import model.QuestionRepository;
import model.SysData;             // <-- your CSV saving / loading
import view.GameView;

/**
 * ==============================================================================
 *   FINAL MERGED GAMECONTROLLER.JAVA  (Option C: with detailed explanations)
 * ==============================================================================
 *
 * This file is a clean merge of:
 *    - YOUR older version (with saveGameToHistory, two-board UI, timer, etc.)
 *    - Your teammates' NEW rewritten version (Board/Cell architecture,
 *      updated flood-fill, question repository, new styling, new CellController,
 *      new rules logic, etc.)
 *
 * WHY WE COULD NOT USE YOUR OLD VERSION:
 *    Your old version relied on:
 *       - CellController.cellModel
 *       - board as CellController[][]
 *       - old CellModel (not used anymore)
 *    Your teammates rewrote ALL of this.
 *
 * WHAT WE KEEP:
 *    ✔ Game history saving
 *    ✔ saveGameToHistory()
 *    ✔ elapsedTime, difficulty, player names
 *
 * WHAT WE REMOVE:
 *    ✘ All old duplicated GameController code
 *    ✘ Old QuestionDialog (now uses new repository)
 *
 * WHAT WE PRESERVE FROM TEAM:
 *    ✔ New architecture (Board, Cell, CellController)
 *    ✔ New UI styling
 *    ✔ New flood-fill rules
 *    ✔ New QuestionDialog using CSV repository
 *    ✔ New event handler mapping
 *
 * ==============================================================================
 */
public class GameController {

    // -------------------------------------------------------------------------
    // FIELDS
    // -------------------------------------------------------------------------

    public int N, M;                // board size
    public GameView gameView;       // UI view
    public GameModel gameModel;     // logical model

    // Board UI (CellController wrappers)
    public CellController[][] board1;
    public CellController[][] board2;

    // Player names (cooperative game)
    public String player1Name = "Player 1";
    public String player2Name = "Player 2";

    // Game state
    private int currentPlayer = 1;  // 1 or 2
    private boolean gameActive = true;

    // Timer
    private Timer gameTimer;
    private int elapsedTime = 0;

    // Difficulty settings
    private String difficulty;
    private int mineCount;
    private int sharedLives;

    // -------------------------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    public GameController(String difficulty, String p1Name, String p2Name) {

        this.difficulty = difficulty;
        this.player1Name = p1Name;
        this.player2Name = p2Name;

        // ---------------- Difficulty Rules (Same for both versions) ----------------
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

    // -------------------------------------------------------------------------
    // INIT (runs at new game + restart)
    // -------------------------------------------------------------------------
    public void init() {

        gameActive = true;
        currentPlayer = 1;
        elapsedTime = 0;

        // Reset boards in the model
        gameModel.initializeBoards(N, M);

        Board logicalBoard1 = gameModel.getBoard1();
        Board logicalBoard2 = gameModel.getBoard2();

        // Build UI wrappers per logical Board
        board1 = createUiBoard(logicalBoard1);
        board2 = createUiBoard(logicalBoard2);

        // Populate the GridPane with UI CellViews
        createCellsGrid(board1, gameView.gridPane1);
        createCellsGrid(board2, gameView.gridPane2);

        // Add mouse interactions
        addEventHandlerToBoard(board1, 1);
        addEventHandlerToBoard(board2, 2);

        updateUI();
        highlightCurrentPlayer();
    }

    // -------------------------------------------------------------------------
    // Create UI Wrappers
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // Place CellView in the GridPane
    // -------------------------------------------------------------------------
    private void createCellsGrid(CellController[][] board, javafx.scene.layout.GridPane gridPane) {

        gridPane.getChildren().clear();

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {

                gridPane.add(board[i][j].cellView, i, j, 1, 1);
            }
        }
    }

    // -------------------------------------------------------------------------
    // EVENT HANDLERS (restart, exit, end turn)
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

        // End turn button
        gameView.endTurnBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (gameActive) {
                switchPlayer();
            }
        });
    }

    // -------------------------------------------------------------------------
    // ADD CELL MOUSE HANDLERS
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

                                // Only if active + correct player's turn
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

    // -------------------------------------------------------------------------
    // LEFT CLICK LOGIC
    // -------------------------------------------------------------------------
    private void handleLeftClick(CellController[][] board, int row, int col, int playerNum) {

        CellController cellCtrl = board[row][col];
        Cell cell = cellCtrl.getCell();

        // SPECIAL CASE 1: Click-to-activate Surprise
        if (cell.isSurprise() && cell.isDiscovered() && !cell.isActivated()) {
            activateSurpriseCell(cellCtrl);
            switchPlayer();
            return;
        }

        // SPECIAL CASE 2: Click-to-activate Question
        if (cell.isQuestion() && cell.isDiscovered() && !cell.isActivated()) {
            activateQuestionCell(cellCtrl);
            switchPlayer();
            return;
        }

        // Ignore if open or flagged
        if (cell.isOpen() || cell.isFlag()) return;

        // Open the cell
        openCell(board, row, col, true);

        // Determine content
        if (cell.isMine()) {

            handleMineHit();
            switchPlayer();

        } else if (cell.isSurprise()) {

            cell.setDiscovered(true);
            cellCtrl.init();
            showMessage("Surprise Cell Discovered!",
                    "You can activate it next turn.");
            switchPlayer();

        } else if (cell.isQuestion()) {

            cell.setDiscovered(true);
            cellCtrl.init();
            showMessage("Question Cell Discovered!",
                    "You can activate it next turn.");
            switchPlayer();

        } else {
            // Normal number / empty cell → turn ends
            switchPlayer();
        }

        updateUI();
        checkWinCondition();
    }

    // -------------------------------------------------------------------------
    // RIGHT CLICK (Flag)
    // -------------------------------------------------------------------------
    private void handleRightClick(CellController[][] board, int row, int col) {

        CellController cellCtrl = board[row][col];
        Cell cell = cellCtrl.getCell();

        // Can't flag open cells
        if (cell.isOpen()) return;

        boolean wasFlagged = cell.isFlag();
        cell.toggleFlag();
        boolean isFlagged = cell.isFlag();

        // Apply scoring ONCE
        if (!wasFlagged && isFlagged && !cell.isFlagScored()) {

            if (cell.isMine()) gameModel.sharedScore += 1;
            else              gameModel.sharedScore -= 3;

            cell.setFlagScored(true);
        }

        cellCtrl.init();
        updateUI();
    }

    // -------------------------------------------------------------------------
    // OPEN CELL (flood-fill logic)
    // -------------------------------------------------------------------------
    private void openCell(CellController[][] board, int row, int col, boolean isRootClick) {

        if (!isInBoard(row, col)) return;

        CellController cellCtrl = board[row][col];
        Cell cell = cellCtrl.getCell();

        if (cell.isOpen() || cell.isFlag()) return;

        // OPEN NOW
        cell.setOpen(true);
        gameModel.revealedCells++;

        boolean isMine = cell.isMine();
        boolean isSpecial = cell.isSpecial();
        int neighbors = cell.getNeighborMinesNum();

        // Mark special cells as discovered
        if (isSpecial && !cell.isDiscovered()) {
            cell.setDiscovered(true);
        }

        // ROOT CLICK SCORE
        if (isRootClick && !isMine && !isSpecial) {
            gameModel.sharedScore += 1;
        }

        // Determine if we expand
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
                "You hit a mine! -1 life.\nShared Lives Remaining: "
                        + gameModel.sharedLives);

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
                    "Need " + cost + " points to activate this Surprise Cell");
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
                int extra = gameModel.sharedLives - 10;
                gameModel.sharedLives = 10;

                int converted = extra *
                        (difficulty.equals("Easy") ? 5 :
                         difficulty.equals("Medium") ? 8 : 12);

                gameModel.sharedScore += converted;

                showMessage("Good Surprise!",
                        "+1 life (max reached, extras → +" + converted +
                        " points)\n+" + bonusPoints + " points");
            } else {
                showMessage("Good Surprise!",
                        "+1 life\n+" + bonusPoints + " points");
            }

        } else {
            gameModel.sharedLives--;
            gameModel.sharedScore -= bonusPoints;

            showMessage("Bad Surprise!",
                    "-1 life\n-" + bonusPoints + " points");

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
    // QUESTION CELL ACTIVATION
    // -------------------------------------------------------------------------
    private void activateQuestionCell(CellController cellCtrl) {

        // Use the NEW QuestionDialog (team version using CSV)
        QuestionDialog dialog = new QuestionDialog(difficulty);

        Optional<Boolean> result = dialog.showAndWait();

        if (result.isPresent()) {
            boolean correct = result.get();
            String qDifficulty = dialog.getQuestionDifficulty();

            applyQuestionReward(correct, qDifficulty);

            Cell cell = cellCtrl.getCell();
            cell.setActivated(true);
            cellCtrl.init();
            updateUI();

            if (gameModel.sharedLives <= 0) {
                endGame(false);
            }
        }
    }

    // -------------------------------------------------------------------------
    // APPLY QUESTION REWARD (Uses same logic as team's version)
    // -------------------------------------------------------------------------
    private void applyQuestionReward(boolean correct, String qDiff) {

        int points = 0;
        int lives  = 0;

        // Uses the official table exactly as in your teammate’s version
        if (difficulty.equals("Easy")) {
            switch (qDiff) {
                case "Easy":         points = correct ? 3  : -3;  lives = correct ? 1 : 0; break;
                case "Intermediate": points = correct ? 6  : -6; break;
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

        // Cap lives at 10
        if (gameModel.sharedLives > 10) {

            int extra = gameModel.sharedLives - 10;
            gameModel.sharedLives = 10;

            int converted = extra *
                    (difficulty.equals("Easy") ? 5 :
                     difficulty.equals("Medium") ? 8 : 12);

            gameModel.sharedScore += converted;
        }

        String msg = correct ?
                "Correct ✓\n+" + Math.abs(points) + " points" +
                (lives > 0 ? ", +" + lives + " lives" : "") :
                "Wrong ✗\n" + points + " points" +
                (lives < 0 ? ", " + lives + " lives" : "");

        showMessage("Question Result",
                msg +
                "\n\nScore: " + gameModel.sharedScore +
                " | Lives: " + gameModel.sharedLives);
    }

    // -------------------------------------------------------------------------
    // SWITCH PLAYER
    // -------------------------------------------------------------------------
    private void switchPlayer() {
        currentPlayer = (currentPlayer == 1 ? 2 : 1);
        highlightCurrentPlayer();
        updateUI();
    }

    // -------------------------------------------------------------------------
    // HIGHLIGHT ACTIVE PLAYER UI
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // UPDATE UI STATE
    // -------------------------------------------------------------------------
    private void updateUI() {

        gameView.sharedScoreLabel.setText("" + gameModel.sharedScore);
        gameView.sharedLivesLabel.setText("" + gameModel.sharedLives);
        gameView.currentPlayerLabel.setText(
                (currentPlayer == 1 ? player1Name : player2Name) + "'s Turn");

        gameView.difficultyLabel.setText(difficulty);
        gameView.timeLabel.setText(formatTime(elapsedTime));

        // Color lives if low
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
    // CHECK WIN CONDITION
    // -------------------------------------------------------------------------
    private void checkWinCondition() {

        int totalCells = N * M * 2;
        int totalMines = mineCount * 2;
        int safeCells = totalCells - totalMines;

        if (gameModel.revealedCells >= safeCells) {
            endGame(true);
        }
    }

    // -------------------------------------------------------------------------
    // START TIMER
    // -------------------------------------------------------------------------
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
    // END GAME (MERGED WITH SAVE-TO-HISTORY)
    // -------------------------------------------------------------------------
    private void endGame(boolean won) {

        gameActive = false;

        if (gameTimer != null)
            gameTimer.cancel();

        // Convert life bonus
        int lifeBonus =
                gameModel.sharedLives *
                        (difficulty.equals("Easy") ? 5 :
                         difficulty.equals("Medium") ? 8 : 12);

        int finalScore = gameModel.sharedScore + lifeBonus;

        // 🎯 ***YOUR MERGED FEATURE*** → Save to CSV
        saveGameToHistory(won, finalScore);

        // ---------------------------------------------
        // Show result alert
        //----------------------------------------------
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

    // -------------------------------------------------------------------------
    // SAVE GAME HISTORY (YOUR METHOD — ADAPTED TO NEW ARCHITECTURE)
    // -------------------------------------------------------------------------
    private void saveGameToHistory(boolean won, int finalScore) {

        // 1. Build date string
        String dateTime =
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // 2. Cooperative result
        String result = won ? "WIN" : "LOSE";

        // 3. Create entry
        GameHistoryEntry entry = new GameHistoryEntry(
                dateTime,
                difficulty,
                player1Name,
                player2Name,
                result,
                finalScore,
                elapsedTime
        );

        // 4. Save using SysData (writes to CSV)
        SysData.saveGame(entry);
    }

    // -------------------------------------------------------------------------
    // SIMPLE MESSAGE
    // -------------------------------------------------------------------------
    private void showMessage(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

} // END OF GAMECONTROLLER


/* =============================================================================
   QUESTION DIALOG (TEAM VERSION — WITH INLINE COMMENTS)
   =============================================================================
   Uses CSV QuestionRepository. This was kept almost exactly the same, but
   formatted and clearly separated from GameController.
   Also compatible with applyQuestionReward() which expects difficulty text
   like "Easy", "Intermediate", "Hard", "Expert".
============================================================================= */

class QuestionDialog {

    private final Dialog<Boolean> dialog;
    private String questionDifficulty;
    private int correctIndex;

    public QuestionDialog(String gameDifficulty) {

        dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Load all questions from CSV
        List<Question> allQuestions = QuestionRepository.loadQuestions();
        Question chosen = null;

        if (allQuestions != null && !allQuestions.isEmpty()) {

            chosen = allQuestions.get(new Random().nextInt(allQuestions.size()));

            QuestionDifficulty diffEnum = chosen.getDifficulty();
            questionDifficulty = mapDifficultyLabel(diffEnum);

            dialog.setTitle("Question Cell");
            dialog.setHeaderText(questionDifficulty + " Question");

        } else {
            // fallback
            String[] diffs = {"Easy", "Intermediate", "Hard", "Expert"};
            questionDifficulty = diffs[new Random().nextInt(diffs.length)];

            dialog.setTitle("Question Cell");
            dialog.setHeaderText(questionDifficulty + " Question");
        }

        VBox box = new VBox(12);
        box.setPadding(new Insets(20));

        String qText;
        String[] answers;

        if (chosen != null) {
            qText = chosen.getText();
            answers = chosen.getOptions();
            correctIndex = chosen.getCorrectIndex();
            if (answers == null || answers.length < 4) {
                answers = new String[]{"A", "B", "C", "D"};
                correctIndex = 0;
            }
        } else {
            qText = "No questions found in CSV.";
            answers = new String[]{"A", "B", "C", "D"};
            correctIndex = 0;
        }

        Label question = new Label(qText);
        question.setWrapText(true);
        question.setMaxWidth(400);
        box.getChildren().add(question);
        box.getChildren().add(new Label(""));

        for (int i = 0; i < 4; i++) {

            final int idx = i;

            Button btn = new Button((char)('A' + i) + ") " + answers[i]);
            btn.setPrefWidth(400);
            btn.setPrefHeight(45);

            btn.setOnAction(e -> {
                dialog.setResult(idx == correctIndex);
                dialog.close();
            });

            box.getChildren().add(btn);
        }

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().setPrefSize(480, 300);
    }

    private String mapDifficultyLabel(QuestionDifficulty diff) {
        switch (diff) {
            case EASY:    return "Easy";
            case MEDIUM:  return "Intermediate";
            case HARD:    return "Hard";
            case EXPERT:  return "Expert";
        }
        return "Easy";
    }

    public String getQuestionDifficulty() {
        return questionDifficulty;
    }

    public Optional<Boolean> showAndWait() {
        return dialog.showAndWait();
    }
}
