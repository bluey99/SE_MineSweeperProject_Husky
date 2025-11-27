package view;

import controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Game screen UI containing:
 * - Player 1 Board
 * - Shared Info Panel
 * - Player 2 Board
 * - Top Title + Current Player
 * - Bottom Buttons (Restart, End Turn, Exit)
 */
public class GameView extends BorderPane {

    private GameController controller;

    // Top Section
    private VBox topSection = new VBox();

    // Center Section (3 columns)
    private HBox centerSection = new HBox();

    // Bottom Section
    private VBox bottomSection = new VBox();

    // Shared Info Panel
    public VBox sharedInfoPanel = new VBox();
    public Label sharedScoreLabel = new Label("0");
    public Label sharedLivesLabel = new Label("0");
    public Label difficultyLabel = new Label("Easy");
    public Label timeLabel = new Label("00:00");
    public Label currentPlayerLabel = new Label("Player 1's Turn");

    // Player 1 Panel + Board Grid
    public VBox player1Panel = new VBox();
    public GridPane gridPane1 = new GridPane();
    private Label player1Label;

    // Player 2 Panel + Board Grid
    public VBox player2Panel = new VBox();
    public GridPane gridPane2 = new GridPane();
    private Label player2Label;

    // Bottom Buttons
    public Button restartBtn = new Button("New Game");
    public Button endTurnBtn = new Button("End Turn");
    public Button exitBtn = new Button("Exit");

    public GameView(GameController controller) {
        this.controller = controller;

        player1Label = new Label(controller.player1Name + "'s Board");
        player2Label = new Label(controller.player2Name + "'s Board");

        setupLayout();
        setupTopSection();
        setupSharedPanel();
        setupPlayer1Panel();
        setupPlayer2Panel();
        setupCenterSection();
        setupBottomSection();
    }

    private void setupLayout() {
        this.setPadding(new Insets(15));
        this.setTop(topSection);
        this.setCenter(centerSection);
        this.setBottom(bottomSection);

        // Background
        BackgroundFill bg = new BackgroundFill(
                Color.web("#ECEFF1"),
                CornerRadii.EMPTY,
                Insets.EMPTY
        );
        setBackground(new Background(bg));
    }

    private void setupTopSection() {
        topSection.setAlignment(Pos.CENTER);
        topSection.setSpacing(8);
        topSection.setPadding(new Insets(10, 0, 15, 0));

        Label titleLabel = new Label("Cooperative Minesweeper");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        titleLabel.setTextFill(Color.web("#1565C0"));

        currentPlayerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        currentPlayerLabel.setTextFill(Color.web("#2E7D32"));

        Label subtitle = new Label("Work together • Share score & lives");
        subtitle.setFont(Font.font("Arial", 13));
        subtitle.setTextFill(Color.web("#616161"));

        topSection.getChildren().addAll(titleLabel, currentPlayerLabel, subtitle);
    }

    private void setupSharedPanel() {
        sharedInfoPanel.setPrefWidth(220);
        sharedInfoPanel.setMinWidth(220);
        sharedInfoPanel.setAlignment(Pos.TOP_CENTER);
        sharedInfoPanel.setSpacing(18);
        sharedInfoPanel.setPadding(new Insets(20));
        sharedInfoPanel.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #1976D2;" +
                "-fx-border-width: 3;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);"
        );

        Label header = new Label("═══ SHARED ═══");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        header.setTextFill(Color.web("#1976D2"));

        // SCORE
        VBox scoreBox = new VBox(5);
        scoreBox.setAlignment(Pos.CENTER);
        Label scoreTitle = new Label("SCORE");
        scoreTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        sharedScoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        sharedScoreLabel.setTextFill(Color.web("#F57C00"));
        scoreBox.getChildren().addAll(scoreTitle, sharedScoreLabel);

        // LIVES
        VBox livesBox = new VBox(5);
        livesBox.setAlignment(Pos.CENTER);
        Label livesTitle = new Label("LIVES");
        livesTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        sharedLivesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        sharedLivesLabel.setTextFill(Color.web("#1976D2"));
        livesBox.getChildren().addAll(livesTitle, sharedLivesLabel);

        Region div1 = new Region();
        div1.setPrefHeight(2);
        div1.setStyle("-fx-background-color: #E0E0E0;");

        // Difficulty + Time
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.CENTER);

        HBox diffBox = new HBox(5);
        diffBox.setAlignment(Pos.CENTER);
        Label diffIcon = new Label("⚙");
        difficultyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        diffBox.getChildren().addAll(diffIcon, difficultyLabel);

        HBox timeBox = new HBox(5);
        timeBox.setAlignment(Pos.CENTER);
        Label timeIcon = new Label("⏱");
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        timeBox.getChildren().addAll(timeIcon, timeLabel);

        infoBox.getChildren().addAll(diffBox, timeBox);

        sharedInfoPanel.getChildren().addAll(
                header,
                scoreBox,
                livesBox,
                div1,
                infoBox
        );
    }

    private void setupPlayer1Panel() {
        player1Panel.setAlignment(Pos.TOP_CENTER);
        player1Panel.setSpacing(10);
        player1Panel.setPadding(new Insets(15));
        player1Panel.setStyle(
                "-fx-border-color: #4CAF50;" +
                "-fx-border-width: 4;" +
                "-fx-background-color: #E8F5E9;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);"
        );

        player1Label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        player1Label.setTextFill(Color.web("#2E7D32"));

        player1Panel.getChildren().addAll(player1Label, gridPane1);
    }

    private void setupPlayer2Panel() {
        player2Panel.setAlignment(Pos.TOP_CENTER);
        player2Panel.setSpacing(10);
        player2Panel.setPadding(new Insets(15));
        player2Panel.setStyle(
                "-fx-border-color: #BDBDBD;" +
                "-fx-border-width: 2;" +
                "-fx-background-color: white;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);"
        );

        player2Label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        player2Label.setTextFill(Color.web("#C62828"));

        player2Panel.getChildren().addAll(player2Label, gridPane2);
    }

    private void setupCenterSection() {
        centerSection.setAlignment(Pos.CENTER);
        centerSection.setSpacing(15);
        centerSection.setPadding(new Insets(10));

        centerSection.getChildren().addAll(player1Panel, sharedInfoPanel, player2Panel);
    }

    private void setupBottomSection() {
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setSpacing(15);
        bottomSection.setPadding(new Insets(10, 0, 15, 0));

        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);

        styleButton(endTurnBtn, "#4CAF50", 140);
        styleButton(restartBtn, "#2196F3", 130);
        styleButton(exitBtn, "#F44336", 100);

        box.getChildren().addAll(endTurnBtn, restartBtn, exitBtn);
        bottomSection.getChildren().add(box);
    }

    private void styleButton(Button btn, String color, int width) {
        btn.setPrefSize(width, 42);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 2);"
        );
    }
}
