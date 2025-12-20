package view;

import controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
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
 * - Bottom Buttons (Restart, Exit, Return to Menu)
 */
public class GameView extends BorderPane {

    private final GameController controller;

    private static final double PANEL_WIDTH = 620;

    private final VBox topSection = new VBox();
    private final HBox centerSection = new HBox();
    private final VBox bottomSection = new VBox();

    public final VBox sharedInfoPanel = new VBox();
    public final Label sharedScoreLabel = new Label("0");
    public final Label sharedLivesLabel = new Label("0");
    public final Label difficultyLabel = new Label("Easy");
    public final Label timeLabel = new Label("00:00");
    public final Label currentPlayerLabel = new Label("Player 1's Turn");

    public final VBox player1Panel = new VBox();
    public final GridPane gridPane1 = new GridPane();
    private final Label player1Label;

    public final VBox player2Panel = new VBox();
    public final GridPane gridPane2 = new GridPane();
    private final Label player2Label;

    public final Button restartBtn = new Button("New Game");
    public final Button exitBtn = new Button("Exit");
    public final Button backToMenuBtn = new Button("Return to Menu");

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
        setPadding(new Insets(10));
        setTop(topSection);
        setCenter(centerSection);
        setBottom(bottomSection);

        setBackground(new Background(new BackgroundFill(
                Color.web("#0F172A"),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
    }

    // ===== TOP SECTION (MineMates logo like Setup UI) =====
    private void setupTopSection() {
        topSection.setAlignment(Pos.CENTER);
        topSection.setSpacing(6);
        topSection.setPadding(new Insets(14, 0, 18, 0));

        // Load same logo font
        Font logoFont = Font.loadFont(
                getClass().getResourceAsStream("/fonts/ka1.ttf"),
                28
        );

        Label titleLabel = new Label("MineMates");
        if (logoFont != null) {
            titleLabel.setFont(logoFont);
        } else {
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        }

        titleLabel.setTextFill(Color.web("#ECFDF5"));

        DropShadow glow = new DropShadow();
        glow.setRadius(8);
        glow.setSpread(0.15);
        glow.setColor(Color.web("#22C55E"));
        titleLabel.setEffect(glow);

        currentPlayerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        currentPlayerLabel.setTextFill(Color.web("#60A5FA"));

        Label subtitle = new Label("Two players • One goal • Shared victory");
        subtitle.setFont(Font.font("Arial", 13));
        subtitle.setTextFill(Color.web("#9CA3AF"));

        topSection.getChildren().addAll(titleLabel, currentPlayerLabel, subtitle);
    }

    // ===== SHARED PANEL =====
    private void setupSharedPanel() {
        sharedInfoPanel.setPrefWidth(220);
        sharedInfoPanel.setMinWidth(220);
        sharedInfoPanel.setAlignment(Pos.TOP_CENTER);
        sharedInfoPanel.setSpacing(14);
        sharedInfoPanel.setPadding(new Insets(16));
        sharedInfoPanel.setStyle(
                "-fx-background-color: #111827;" +
                "-fx-border-color: #1D4ED8;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 16;" +
                "-fx-background-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 18, 0, 0, 8);"
        );

        Label header = new Label("═══ SHARED ═══");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        header.setTextFill(Color.web("#BFDBFE"));

        VBox scoreBox = new VBox(4);
        scoreBox.setAlignment(Pos.CENTER);
        Label scoreTitle = new Label("SCORE");
        scoreTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        scoreTitle.setTextFill(Color.web("#9CA3AF"));

        sharedScoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        sharedScoreLabel.setTextFill(Color.web("#FDBA74"));
        scoreBox.getChildren().addAll(scoreTitle, sharedScoreLabel);

        VBox livesBox = new VBox(4);
        livesBox.setAlignment(Pos.CENTER);
        Label livesTitle = new Label("LIVES");
        livesTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        livesTitle.setTextFill(Color.web("#9CA3AF"));

        sharedLivesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        sharedLivesLabel.setTextFill(Color.web("#A5B4FC"));
        livesBox.getChildren().addAll(livesTitle, sharedLivesLabel);

        Region div1 = new Region();
        div1.setPrefHeight(1.5);
        div1.setStyle("-fx-background-color: #1F2937;");

        VBox infoBox = new VBox(6);
        infoBox.setAlignment(Pos.CENTER);

        HBox diffBox = new HBox(6);
        diffBox.setAlignment(Pos.CENTER);
        Label diffIcon = new Label("⚙");
        diffIcon.setTextFill(Color.web("#9CA3AF"));
        difficultyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        difficultyLabel.setTextFill(Color.web("#E5E7EB"));
        diffBox.getChildren().addAll(diffIcon, difficultyLabel);

        HBox timeBox = new HBox(6);
        timeBox.setAlignment(Pos.CENTER);
        Label timeIcon = new Label("⏱");
        timeIcon.setTextFill(Color.web("#9CA3AF"));
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        timeLabel.setTextFill(Color.web("#E5E7EB"));
        timeBox.getChildren().addAll(timeIcon, timeLabel);

        infoBox.getChildren().addAll(diffBox, timeBox);

        sharedInfoPanel.getChildren().addAll(
                header, scoreBox, livesBox, div1, infoBox
        );
    }

    private void setupPlayer1Panel() {
        player1Panel.setAlignment(Pos.TOP_CENTER);
        player1Panel.setSpacing(10);
        player1Panel.setPadding(new Insets(16));
        player1Panel.setPrefWidth(PANEL_WIDTH);
        player1Panel.setStyle(
                "-fx-border-color: #22C55E;" +
                "-fx-border-width: 2.5;" +
                "-fx-background-color: #111827;" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;"
        );

        player1Label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        player1Label.setTextFill(Color.web("#DCFCE7"));

        gridPane1.setHgap(3);
        gridPane1.setVgap(3);
        gridPane1.setAlignment(Pos.CENTER);

        StackPane boardContainer1 = new StackPane();
        boardContainer1.setStyle("-fx-background-color: #020617; -fx-background-radius: 10;");
        boardContainer1.setPadding(new Insets(2));
        boardContainer1.getChildren().add(gridPane1);

        player1Panel.getChildren().addAll(player1Label, boardContainer1);
    }

    private void setupPlayer2Panel() {
        player2Panel.setAlignment(Pos.TOP_CENTER);
        player2Panel.setSpacing(10);
        player2Panel.setPadding(new Insets(16));
        player2Panel.setPrefWidth(PANEL_WIDTH);
        player2Panel.setStyle(
                "-fx-border-color: #374151;" +
                "-fx-border-width: 2;" +
                "-fx-background-color: #111827;" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;"
        );

        player2Label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        player2Label.setTextFill(Color.web("#FCA5A5"));

        gridPane2.setHgap(3);
        gridPane2.setVgap(3);
        gridPane2.setAlignment(Pos.CENTER);

        StackPane boardContainer2 = new StackPane();
        boardContainer2.setStyle("-fx-background-color: #020617; -fx-background-radius: 10;");
        boardContainer2.setPadding(new Insets(2));
        boardContainer2.getChildren().add(gridPane2);

        player2Panel.getChildren().addAll(player2Label, boardContainer2);
    }

    private void setupCenterSection() {
        centerSection.setAlignment(Pos.CENTER);
        centerSection.setSpacing(16);
        centerSection.setPadding(new Insets(8, 4, 16, 4));
        centerSection.getChildren().addAll(player1Panel, sharedInfoPanel, player2Panel);
    }

    private void setupBottomSection() {
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setPadding(new Insets(0, 0, 14, 0));

        HBox box = new HBox(14);
        box.setAlignment(Pos.CENTER);

        styleButton(restartBtn, "#2563EB", 140);
        styleButton(exitBtn, "#EF4444", 110);
        styleButton(backToMenuBtn, "#64748B", 150);

        box.getChildren().addAll(restartBtn, exitBtn, backToMenuBtn);
        bottomSection.getChildren().add(box);
    }

    private void styleButton(Button btn, String color, int width) {
        btn.setPrefSize(width, 40);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 999;" +
                "-fx-cursor: hand;"
        );
    }
}
