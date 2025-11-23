package view;

import controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Main game view for cooperative two-player Minesweeper
 */
public class GameView extends BorderPane {
    
    private GameController controller;
    
    // Main layout sections
    private VBox topSection = new VBox();
    private HBox centerSection = new HBox();
    private VBox bottomSection = new VBox();
    
    // Shared game info panel (center)
    private VBox sharedInfoPanel = new VBox();
    public Label sharedScoreLabel = new Label("0");
    public Label sharedLivesLabel = new Label("0");
    public Label difficultyLabel = new Label("Easy");
    public Label timeLabel = new Label("00:00");
    public Label currentPlayerLabel = new Label("Player 1's Turn");
    
    // Player 1 board (left)
    public VBox player1Panel = new VBox();
    public GridPane gridPane1 = new GridPane();
    private Label player1Label;
    
    // Player 2 board (right)
    public VBox player2Panel = new VBox();
    public GridPane gridPane2 = new GridPane();
    private Label player2Label;
    
    // Control buttons
    public Button restartBtn = new Button("New Game");
    public Button endTurnBtn = new Button("End Turn");
    public Button exitBtn = new Button("Exit");

    public GameView(GameController controller) {
        this.controller = controller;
        
        player1Label = new Label(controller.player1Name + "'s Board");
        player2Label = new Label(controller.player2Name + "'s Board");
        
        setupLayout();
        setupTopSection();
        setupSharedInfoPanel();
        setupPlayer1Panel();
        setupPlayer2Panel();
        setupCenterSection();
        setupBottomSection();
    }

    private void setupLayout() {
        this.setTop(topSection);
        this.setCenter(centerSection);
        this.setBottom(bottomSection);
        this.setPadding(new Insets(15));
        
        // Gradient background
        BackgroundFill bgFill = new BackgroundFill(
            Color.web("#ECEFF1"), 
            CornerRadii.EMPTY, 
            Insets.EMPTY
        );
        this.setBackground(new Background(bgFill));
    }

    private void setupTopSection() {
        topSection.setAlignment(Pos.CENTER);
        topSection.setSpacing(8);
        topSection.setPadding(new Insets(10, 0, 15, 0));
        
        // Game title
        Label titleLabel = new Label("⚑ Cooperative Minesweeper ⚑");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        titleLabel.setTextFill(Color.web("#1565C0"));
        
        // Current player indicator
        currentPlayerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        currentPlayerLabel.setTextFill(Color.web("#2E7D32"));
        
        // Subtitle
        Label subtitleLabel = new Label("Work together • Share score & lives");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        subtitleLabel.setTextFill(Color.web("#616161"));
        
        topSection.getChildren().addAll(titleLabel, currentPlayerLabel, subtitleLabel);
    }

    private void setupSharedInfoPanel() {
        sharedInfoPanel.setAlignment(Pos.TOP_CENTER);
        sharedInfoPanel.setSpacing(18);
        sharedInfoPanel.setPadding(new Insets(20, 15, 20, 15));
        sharedInfoPanel.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #1976D2;" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);"
        );
        sharedInfoPanel.setPrefWidth(220);
        sharedInfoPanel.setMinWidth(220);
        
        // Shared label header
        Label sharedHeader = new Label("═══ SHARED ═══");
        sharedHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        sharedHeader.setTextFill(Color.web("#1976D2"));
        
        // Score display
        VBox scoreBox = new VBox(5);
        scoreBox.setAlignment(Pos.CENTER);
        Label scoreLabel = new Label("SCORE");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        scoreLabel.setTextFill(Color.web("#757575"));
        sharedScoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        sharedScoreLabel.setTextFill(Color.web("#F57C00"));
        scoreBox.getChildren().addAll(scoreLabel, sharedScoreLabel);
        
        // Lives display
        VBox livesBox = new VBox(5);
        livesBox.setAlignment(Pos.CENTER);
        Label livesLabel = new Label("LIVES");
        livesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        livesLabel.setTextFill(Color.web("#757575"));
        sharedLivesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        sharedLivesLabel.setTextFill(Color.web("#1976D2"));
        livesBox.getChildren().addAll(livesLabel, sharedLivesLabel);
        
        // Divider line
        Region divider1 = new Region();
        divider1.setPrefHeight(2);
        divider1.setStyle("-fx-background-color: #E0E0E0;");
        
        // Game info
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.CENTER);
        
        HBox diffBox = new HBox(5);
        diffBox.setAlignment(Pos.CENTER);
        Label diffIcon = new Label("⚙");
        diffIcon.setFont(Font.font(16));
        difficultyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        diffBox.getChildren().addAll(diffIcon, difficultyLabel);
        
        HBox timeBox = new HBox(5);
        timeBox.setAlignment(Pos.CENTER);
        Label timeIcon = new Label("⏱");
        timeIcon.setFont(Font.font(16));
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        timeBox.getChildren().addAll(timeIcon, timeLabel);
        
        infoBox.getChildren().addAll(diffBox, timeBox);
        
        // Divider line
        Region divider2 = new Region();
        divider2.setPrefHeight(2);
        divider2.setStyle("-fx-background-color: #E0E0E0;");
        
        // Instructions
        Label instructionsLabel = new Label(
            "Controls:\n" +
            "━━━━━━━\n" +
            "Left Click:\n  Reveal cell\n\n" +
            "Right Click:\n  Flag mine\n\n" +
            "━━━━━━━\n" +
            "Correct flag: +1\n" +
            "Wrong flag: -3\n" +
            "Hit mine: -1 life"
        );
        instructionsLabel.setFont(Font.font("Arial", 11));
        instructionsLabel.setTextFill(Color.web("#616161"));
        instructionsLabel.setStyle("-fx-text-alignment: center; -fx-line-spacing: 2px;");
        instructionsLabel.setWrapText(true);
        instructionsLabel.setMaxWidth(190);
        
        sharedInfoPanel.getChildren().addAll(
            sharedHeader,
            scoreBox,
            livesBox,
            divider1,
            infoBox,
            divider2,
            instructionsLabel
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
        bottomSection.setPadding(new Insets(15, 0, 5, 0));
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        styleButton(endTurnBtn, "#4CAF50", 140);
        styleButton(restartBtn, "#2196F3", 130);
        styleButton(exitBtn, "#F44336", 100);
        
        buttonBox.getChildren().addAll(endTurnBtn, restartBtn, exitBtn);
        bottomSection.getChildren().add(buttonBox);
    }

    private void styleButton(Button button, String color, int width) {
        button.setPrefSize(width, 42);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 2);"
        );
        
        button.setOnMouseEntered(e -> 
            button.setStyle(
                "-fx-background-color: derive(" + color + ", -15%);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 0, 3);" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
            )
        );
        
        button.setOnMouseExited(e -> 
            button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 2);" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
            )
        );
    }
}