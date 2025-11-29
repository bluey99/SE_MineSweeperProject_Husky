package controller;

import javafx.application.Platform;
import model.GameHistoryEntry;
import model.SysData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.GameModel;
import view.GameView;

import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main game controller for cooperative two-player Minesweeper
 * Manages turn-based gameplay with shared score and lives
 */
public class GameController {
    public int N, M;
    public GameView gameView;
    public GameModel gameModel;
    
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
        switch(difficulty) {
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
        
        // Initialize both player boards
        gameModel.initializeBoards(N, M);
        
        // Create cell grids in view
        createCellsGrid(gameModel.board1, gameView.gridPane1);
        createCellsGrid(gameModel.board2, gameView.gridPane2);
        
        // Add event handlers to cells
        addEventHandlerToBoard(gameModel.board1, 1);
        addEventHandlerToBoard(gameModel.board2, 2);
        
        updateUI();
        highlightCurrentPlayer();
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
        CellController cell = board[row][col];

        // 1) If it's a discovered special cell and not yet activated -> activate it
        if (cell.cellModel.isSurprise() && cell.cellModel.isDiscovered() && !cell.cellModel.isActivated()) {
            activateSurpriseCell(cell);
            switchPlayer();
            return;
        }

        if (cell.cellModel.isQuestion() && cell.cellModel.isDiscovered() && !cell.cellModel.isActivated()) {
            activateQuestionCell(cell);
            switchPlayer();
            return;
        }
        
        // 2) If cell is already open or flagged (and not a "click to activate" special), ignore
        if (cell.cellModel.isOpen() || cell.cellModel.isFlag()) return;
        
        // 3) Open the cell (root click)
        openCell(board, row, col, true);
        
        // 4) Check cell type and handle accordingly
        if (cell.cellModel.isMine()) {
            handleMineHit();
            switchPlayer();
        } else if (cell.cellModel.isSurprise()) {
            cell.cellModel.setDiscovered(true);
            cell.init();
            showMessage("Surprise Cell Discovered!", 
                "A Surprise Cell has been found!\nYou can activate it on your next turn.");
            switchPlayer(); // ends turn (SRS 3.2.18)
        } else if (cell.cellModel.isQuestion()) {
            cell.cellModel.setDiscovered(true);
            cell.init();
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
        CellController cell = board[row][col];
        
        // Can only flag unopened cells
        if (cell.cellModel.isOpen()) return;
        
        boolean wasFlagged = cell.cellModel.isFlag();
        cell.cellModel.toggleFlag();
        boolean isFlagged = cell.cellModel.isFlag();

        // Apply flagging rewards/penalties ONCE per cell (SRS 3.2.10–3.2.13)
        if (!wasFlagged && isFlagged && !cell.cellModel.isFlagScored()) {
            if (cell.cellModel.isMine()) {
                gameModel.sharedScore += 1;   // +1 for correct flag
            } else {
                gameModel.sharedScore -= 3;   // -3 for incorrect flag
            }
            cell.cellModel.setFlagScored(true); // prevent double scoring
        }

        cell.init();
        updateUI();
        // Flagging doesn't end turn (SRS 3.2.9)
    }

 // Simple version kept for compatibility if you ever call it elsewhere
    private void openCell(CellController[][] board, int row, int col) {
        openCell(board, row, col, true);
    }

    /**
     * Open a cell.
     * @param isRootClick true if this was the cell the player actually clicked.
     *                    Only the root clicked non-special cell gives +1 point.
     */
    private void openCell(CellController[][] board, int row, int col, boolean isRootClick) {
        CellController cell = board[row][col];

        // Already open or flagged? do nothing
        if (cell.cellModel.isOpen() || cell.cellModel.isFlag()) return;

        cell.cellModel.setOpen(true);
        gameModel.revealedCells++;

        boolean isSpecial = cell.cellModel.isMine()
                || cell.cellModel.isSurprise()
                || cell.cellModel.isQuestion();

        // 🆕 IMPORTANT:
        // If a Surprise / Question cell is revealed (even via cascade),
        // mark it as discovered so the next click can activate it.
        if (cell.cellModel.isSurprise() || cell.cellModel.isQuestion()) {
            cell.cellModel.setDiscovered(true);
        }

        // Scoring: +1 point for the clicked non-special cell (SRS 3.2.16, 3.2.17)
        if (isRootClick && !isSpecial) {
            gameModel.sharedScore += 1;
        }

        // Flood fill for empty cells: reveal connected empties + bordering numbers (SRS 3.2.17)
        if (!isSpecial && cell.cellModel.getNeighborMinesNum() == 0) {
            for (int i = row - 1; i <= row + 1; i++) {
                for (int j = col - 1; j <= col + 1; j++) {
                    if (isInBoard(i, j) && !(i == row && j == col)) {
                        openCell(board, i, j, false); // cascade: no extra scoring
                    }
                }
            }
        }

        cell.init();
    }


    private void handleMineHit() {
        gameModel.sharedLives--; // SRS 3.2.15
        showMessage("Mine Hit!", 
            "You hit a mine! -1 life\n\nShared Lives Remaining: " + gameModel.sharedLives);
        
        if (gameModel.sharedLives <= 0) {
            endGame(false);
        }
    }

    private void activateSurpriseCell(CellController cell) {
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
        
        cell.cellModel.setActivated(true);
        cell.init();
        updateUI();
    }

    private void activateQuestionCell(CellController cell) {
        // Show question dialog
        QuestionDialog dialog = new QuestionDialog(difficulty);
        Optional<Boolean> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            boolean correct = result.get();
            String questionDifficulty = dialog.getQuestionDifficulty();
            
            applyQuestionReward(correct, questionDifficulty);
            cell.cellModel.setActivated(true);
            cell.init();
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
        String activeBorder = "-fx-border-color: #4CAF50; -fx-border-width: 4; -fx-padding: 10; -fx-background-color: #E8F5E9; -fx-border-radius: 8; -fx-background-radius: 8;";
        String inactiveBorder = "-fx-border-color: #BDBDBD; -fx-border-width: 2; -fx-padding: 10; -fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8;";
        
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
            gameView.sharedLivesLabel.setTextFill(Color.web("#D32F2F"));
        } else {
            gameView.sharedLivesLabel.setTextFill(Color.web("#1976D2"));
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
        
        saveGameToHistory(won, finalScore);
        
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
    
    private void saveGameToHistory(boolean won, int finalScore) {
        // 1. Date & time
        String dateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // 2. Result and winner
        String result = won ? "WIN" : "LOSE";


        // 3. Game length (from your existing timer)
        int gameLengthSeconds = elapsedTime;

        // 4. Build entry
        GameHistoryEntry entry = new GameHistoryEntry(
                dateTime,
                difficulty,      // make sure 'difficulty' is a String field in this controller
                player1Name,     // same for player1Name / player2Name
                player2Name,
                result,
                finalScore,
                gameLengthSeconds
        );

        // 5. Save to CSV using SysData
        SysData.saveGame(entry);
    }

}

/**
 * Question Dialog for Question Cells
 */
class QuestionDialog {
    private String questionDifficulty;
    private boolean correctAnswer = false;
    private boolean answered = false;
    
    public QuestionDialog(String gameDifficulty) {
        // Randomly select question difficulty
        String[] difficulties = {"Easy", "Intermediate", "Hard", "Expert"};
        questionDifficulty = difficulties[new Random().nextInt(difficulties.length)];
    }
    
    public String getQuestionDifficulty() {
        return questionDifficulty;
    }
    
    public Optional<Boolean> showAndWait() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Question Cell");
        alert.setHeaderText(questionDifficulty + " Question");
        
        // Sample question (in real game, load from CSV)
        String question = getSampleQuestion(questionDifficulty);
        String[] answers = getSampleAnswers(questionDifficulty);
        int correctIndex = 1; // Second answer is correct
        
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        
        Label questionLabel = new Label(question);
        questionLabel.setWrapText(true);
        questionLabel.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
        questionLabel.setMaxWidth(400);
        content.getChildren().add(questionLabel);
        
        content.getChildren().add(new Label("")); // Spacer
        
        Button[] buttons = new Button[4];
        for (int i = 0; i < 4; i++) {
            final int index = i;
            buttons[i] = new Button((char)('A' + i) + ")  " + answers[i]);
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
            buttons[i].setOnAction(e -> {
                correctAnswer = (index == correctIndex);
                answered = true;
                alert.close();
            });
            content.getChildren().add(buttons[i]);
        }
        
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().getButtonTypes().clear();
        alert.getDialogPane().setPrefSize(480, 400);
        
        alert.showAndWait();
        
        return answered ? Optional.of(correctAnswer) : Optional.empty();
    }
    
    private String getSampleQuestion(String difficulty) {
        // Sample questions - in real game, load from CSV
        switch (difficulty) {
            case "Easy":
                return "What is 5 + 3?";
            case "Intermediate":
                return "What is the capital of France?";
            case "Hard":
                return "In what year did World War II end?";
            case "Expert":
                return "What is the speed of light in vacuum (in km/s)?";
            default:
                return "Sample question";
        }
    }
    
    private String[] getSampleAnswers(String difficulty) {
        switch (difficulty) {
            case "Easy":
                return new String[]{"7", "8", "9", "10"};
            case "Intermediate":
                return new String[]{"London", "Paris", "Berlin", "Madrid"};
            case "Hard":
                return new String[]{"1944", "1945", "1946", "1947"};
            case "Expert":
                return new String[]{"299,792", "300,000", "250,000", "350,000"};
            default:
                return new String[]{"A", "B", "C", "D"};
        }
    }
    
    


}
