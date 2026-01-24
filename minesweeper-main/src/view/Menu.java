package view;

import controller.Main;
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
import view.dialogs.SettingsDialog;


public class Menu extends StackPane {

    public final Button startBtn = new Button("Start Game");
    public final Button multiplayerBtn = new Button("Multiplayer (LAN)");
    public final Button historyBtn = new Button("History");
    public final Button questionManagementBtn = new Button("Question Management");
    public final Button settingsBtn = new Button("⚙");

    public Menu() {

        // === BACKGROUND IMAGE ===
        Image bg = new Image( "/backgrounds/menuBackground.png");

        BackgroundSize bgSize = new BackgroundSize(
                100, 100, true, true, false, true
        );

        setBackground(new Background(
                new BackgroundImage(
                        bg,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        bgSize
                )
        ));

        // === OVERLAY ROOT ===
        BorderPane overlay = new BorderPane();
        overlay.setStyle(
                "-fx-background-color: rgba(3,10,25,0.82);" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 30, 0, 0, 10);"
        );

        // =========================
        // TOP BAR (SETTINGS)
        // =========================
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(18));
        topBar.setAlignment(Pos.TOP_RIGHT);

        styleSettingsButton(settingsBtn);
        topBar.getChildren().add(settingsBtn);

        overlay.setTop(topBar);

        // =========================
        // CENTER CONTENT
        // =========================
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(80, 0, 100, 0));

        // -------- TITLE --------
        Label title = new Label("MineMates");

        Font arcade = Font.loadFont(
                getClass().getResourceAsStream("/fonts/ka1.ttf"),
                78
        );

        title.setFont(arcade != null
                ? arcade
                : Font.font("Arial", FontWeight.EXTRA_BOLD, 52)
        );

        title.setTextFill(Color.web("#EAFBF4"));

        DropShadow glow = new DropShadow(18, Color.web("#22C55E"));
        glow.setSpread(0.25);
        title.setEffect(glow);

        // -------- SUBTITLE --------
        Label subtitle = new Label("Two players • One goal • Shared victory");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#C7D2FE"));

        // -------- BUTTONS --------
        stylePrimaryButton(startBtn);
        styleSecondaryButton(multiplayerBtn);
        styleSecondaryButton(historyBtn);
        styleSecondaryButton(questionManagementBtn);
        
        settingsBtn.setOnAction(e ->new SettingsDialog().show());
        startBtn.setOnAction(e -> Main.showSetup(Main.getPrimaryStage()));
        multiplayerBtn.setOnAction(e -> Main.showMultiplayerSetup(Main.getPrimaryStage()));
        historyBtn.setOnAction(e -> Main.showHistory(Main.getPrimaryStage()));
        questionManagementBtn.setOnAction(e -> Main.showQuestionManagement(Main.getPrimaryStage()));

        VBox btnContainer = new VBox(15, 
                startBtn,
                multiplayerBtn,
                historyBtn,
                questionManagementBtn
        );
        btnContainer.setAlignment(Pos.CENTER);

        content.getChildren().addAll(title, subtitle, btnContainer);
        overlay.setCenter(content);


        getChildren().add(overlay);
    }

    // =========================
    // STYLES
    // =========================

    private void stylePrimaryButton(Button btn) {
        btn.setPrefSize(240, 48);
        btn.setMinSize(240, 48);
        btn.setMaxSize(240, 48);

        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setStyle(
                "-fx-background-color: linear-gradient(#22C55E, #16A34A);" +
                "-fx-text-fill: #ECFDF5;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-background-color: linear-gradient(#4ADE80, #22C55E);" +
                        "-fx-text-fill: #ECFDF5;" +
                        "-fx-background-radius: 14;" +
                        "-fx-cursor: hand;"
                )
        );

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-background-color: linear-gradient(#22C55E, #16A34A);" +
                        "-fx-text-fill: #ECFDF5;" +
                        "-fx-background-radius: 14;" +
                        "-fx-cursor: hand;"
                )
        );
    }

    private void styleSecondaryButton(Button btn) {
        btn.setPrefSize(240, 48);
        btn.setMinSize(240, 48);
        btn.setMaxSize(240, 48);

        btn.setFont(Font.font("Arial", 15));
        btn.setStyle(
                "-fx-background-color: rgba(15,23,42,0.90);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-background-color: rgba(34,197,94,0.35);" +
                        "-fx-border-color: rgba(34,197,94,0.65);" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-width: 1.2;" +
                        "-fx-text-fill: white;"
                )
        );

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-background-color: rgba(15,23,42,0.90);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 14;"
                )
        );
    }

    private void styleSettingsButton(Button btn) {
        btn.setPrefSize(42, 42);
        btn.setMinSize(42, 42);
        btn.setMaxSize(42, 42);

        btn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        btn.setStyle(
                "-fx-background-color: rgba(15,23,42,0.85);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 999;" +
                "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-background-color: rgba(34,197,94,0.6);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 999;"
                )
        );

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-background-color: rgba(15,23,42,0.85);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 999;"
                )
        );
    }
}
