package view;

import controller.GameController;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
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
    public final Label player1MinesLeftLabel = new Label("Mines Left: 0");

    public final VBox player2Panel = new VBox();
    public final GridPane gridPane2 = new GridPane();
    private final Label player2Label;
    public final Label player2MinesLeftLabel = new Label("Mines Left: 0");

    public final Button restartBtn = new Button("New Game");
    public final Button exitBtn = new Button("Exit");
    public final Button backToMenuBtn = new Button("Return to Menu");

    // containers so we can theme them too
    private final StackPane boardContainer1 = new StackPane();
    private final StackPane boardContainer2 = new StackPane();

    public GameView(GameController controller) {

        player1Label = new Label(controller.player1Name + "'s Board");
        player2Label = new Label(controller.player2Name + "'s Board");

        setupLayout();
        setupTopSection();
        setupSharedPanel();
        setupPlayer1Panel();
        setupPlayer2Panel();
        setupCenterSection();
        setupBottomSection();
        player1Panel.getStyleClass().add("player-panel");
        player2Panel.getStyleClass().add("player-panel");

        player1Panel.getStyleClass().add("p1");
        player2Panel.getStyleClass().add("p2");


        // Theme last
        applyTheme(pickTheme(controller));
        applyGlassPanels(); // ✅ force shared-like glass on player panels too
    }

    private void setupLayout() {
        setPadding(Insets.EMPTY);

        setTop(topSection);
        setCenter(centerSection);
        setBottom(bottomSection);

        // ✅ make sure the BorderPane really fills the scene
        setMinSize(0, 0);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // ✅ remove any background gaps on sections
        topSection.setBackground(Background.EMPTY);
        centerSection.setBackground(Background.EMPTY);
        bottomSection.setBackground(Background.EMPTY);

        // fallback color if image fails
        setBackground(new Background(new BackgroundFill(
                Color.web("#0F172A"),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
    }


    // ===== TOP SECTION =====
    private void setupTopSection() {
        topSection.setAlignment(Pos.CENTER);
        topSection.setSpacing(6);
        topSection.setPadding(new Insets(14, 0, 18, 0));

        Font logoFont = Font.loadFont(
                getClass().getResourceAsStream("/fonts/ka1.ttf"),
                28
        );

        Label titleLabel = new Label("MineMates");
        if (logoFont != null) titleLabel.setFont(logoFont);
        else titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));

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
        DropShadow subtitleShadow = new DropShadow();
        subtitleShadow.setRadius(7);
        subtitleShadow.setOffsetY(2);
        subtitleShadow.setColor(Color.rgb(0, 0, 0, 0.60)); // stronger contrast on bright bg
        subtitle.setEffect(subtitleShadow);

        topSection.getChildren().addAll(titleLabel, currentPlayerLabel, subtitle);
    }

    // ===== SHARED PANEL =====
    private void setupSharedPanel() {
        sharedInfoPanel.setPrefWidth(220);
        sharedInfoPanel.setMinWidth(220);
        sharedInfoPanel.setAlignment(Pos.TOP_CENTER);
        sharedInfoPanel.setSpacing(14);
        sharedInfoPanel.setPadding(new Insets(16));

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
        div1.setMaxWidth(Double.MAX_VALUE);
        div1.setStyle("-fx-background-color: rgba(31,41,55,0.9);");

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

        sharedInfoPanel.getChildren().addAll(header, scoreBox, livesBox, div1, infoBox);
    }

    private void setupPlayer1Panel() {
        player1Panel.setAlignment(Pos.TOP_CENTER);
        player1Panel.setSpacing(10);
        player1Panel.setPadding(new Insets(16));
        player1Panel.setPrefWidth(PANEL_WIDTH);
       

        player1Label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        player1Label.setTextFill(Color.web("#DCFCE7"));

        player1MinesLeftLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        player1MinesLeftLabel.setTextFill(Color.web("#93C5FD"));

        HBox header = new HBox(12, player1Label, player1MinesLeftLabel);
        header.setAlignment(Pos.CENTER);

        // ✅ divider like SHARED
        Region div = new Region();
        div.setPrefHeight(1.5);
        div.setMaxWidth(Double.MAX_VALUE);
        div.setStyle("-fx-background-color: rgba(31,41,55,0.9);");

        gridPane1.setHgap(3);
        gridPane1.setVgap(3);
        gridPane1.setAlignment(Pos.CENTER);

        boardContainer1.getChildren().clear();
        boardContainer1.setPadding(new Insets(10));
        boardContainer1.getChildren().add(gridPane1);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        player1Panel.getChildren().setAll(header, div, boardContainer1, spacer);
    }

    private void setupPlayer2Panel() {
        player2Panel.setAlignment(Pos.TOP_CENTER);
        player2Panel.setSpacing(10);
        player2Panel.setPadding(new Insets(16));
        player2Panel.setPrefWidth(PANEL_WIDTH);

        player2Label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        player2Label.setTextFill(Color.web("#FCA5A5"));

        player2MinesLeftLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        player2MinesLeftLabel.setTextFill(Color.web("#93C5FD"));

        HBox header = new HBox(12, player2Label, player2MinesLeftLabel);
        header.setAlignment(Pos.CENTER);

        // ✅ divider like SHARED
        Region div = new Region();
        div.setPrefHeight(1.5);
        div.setMaxWidth(Double.MAX_VALUE);
        div.setStyle("-fx-background-color: rgba(31,41,55,0.9);");

        gridPane2.setHgap(3);
        gridPane2.setVgap(3);
        gridPane2.setAlignment(Pos.CENTER);

        boardContainer2.getChildren().clear();
        boardContainer2.setPadding(new Insets(10));
        boardContainer2.getChildren().add(gridPane2);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        player2Panel.getChildren().setAll(header, div, boardContainer2, spacer);
    }

    private void setupCenterSection() {
        centerSection.setAlignment(Pos.CENTER);
        centerSection.setSpacing(16);
        centerSection.setPadding(new Insets(8, 4, 16, 4));
        centerSection.getChildren().addAll(player1Panel, sharedInfoPanel, player2Panel);

        centerSection.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        player1Panel.setMaxHeight(Double.MAX_VALUE);
        player2Panel.setMaxHeight(Double.MAX_VALUE);
        sharedInfoPanel.setMaxHeight(Double.MAX_VALUE);

        HBox.setHgrow(player1Panel, Priority.ALWAYS);
        HBox.setHgrow(player2Panel, Priority.ALWAYS);
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

    // ===================== THEME =====================

    private Theme pickTheme(GameController controller) {
        return switch (controller.getDifficulty()) {
            case "Easy" -> Theme.EASY_SEA;
            case "Medium" -> Theme.MED_FOREST;
            case "Hard" -> Theme.HARD_LAVA;
            default -> Theme.EASY_SEA;
        };
    }


    private void applyTheme(Theme t) {

        var url = getClass().getResource(t.bgPath);

        if (url == null) {
            System.out.println("❌ Background not found: " + t.bgPath);
            setBackground(new Background(new BackgroundFill(
                    Color.web("#0F172A"),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));
        } else {
            Image bg = new Image(url.toExternalForm(), false);

            BackgroundImage bgImg = new BackgroundImage(
                    bg,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(
                            100, 100,
                            true, true,
                            true, false
                    )
            );

            BackgroundFill overlay = new BackgroundFill(
                    t.overlay,
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            );

            setBackground(new Background(
                    new BackgroundFill[]{overlay},
                    new BackgroundImage[]{bgImg}
            ));
        }

        // Only text accents here (do not override glass cards)
        currentPlayerLabel.setTextFill(Color.web(t.accent));
    }

    /**
     * ✅ Force all panels to use the same "Shared-like" glass card look
     * and make board containers lighter so they don't look opaque.
     */
    private void applyGlassPanels() {

        String cardBase =
                "-fx-background-color: rgba(17,24,39,0.42);" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 16;" +
                "-fx-background-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 18, 0, 0, 8);";

        sharedInfoPanel.setStyle(cardBase + "-fx-border-color: rgba(96,165,250,0.95);");
        player1Panel.setStyle(cardBase + "-fx-border-color: rgba(34,197,94,0.95);");
        player2Panel.setStyle(cardBase + "-fx-border-color: rgba(244,63,94,0.90);");
        gridPane1.setStyle("-fx-background-color: transparent;");
        gridPane2.setStyle("-fx-background-color: transparent;");


        // Lighter inner glass (so background shows through more)
        String innerGlass =
                "-fx-background-color: transparent;" +
                "-fx-border-color: rgba(255,255,255,0.08);" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;";


        boardContainer1.setStyle(innerGlass);
        boardContainer2.setStyle(innerGlass);
    }
    
    public void setActivePlayer(int playerNum) {
        player1Panel.getStyleClass().remove("active");
        player2Panel.getStyleClass().remove("active");

        if (playerNum == 1) player1Panel.getStyleClass().add("active");
        else player2Panel.getStyleClass().add("active");
    }
    
  
}
