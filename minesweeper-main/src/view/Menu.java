package view;

import controller.Main;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Menu extends StackPane {

    public final Button startBtn = new Button("Start Game");
    public final Button historyBtn = new Button("History");
    public final Button questionManagementBtn = new Button("Question Management");

    public Menu() {

        // === BACKGROUND IMAGE ===
        Image bg = new Image("img/menuBackground.jpg");

        BackgroundSize bgSize = new BackgroundSize(
                100, 100,
                true, true,
                false, true
        );

        BackgroundImage bgImage = new BackgroundImage(
                bg,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                bgSize
        );

        this.setBackground(new Background(bgImage));

        // === FULL-SCREEN OVERLAY PANEL ===
        StackPane overlay = new StackPane();
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());

        overlay.setStyle(
                "-fx-background-color: rgba(3,10,25,0.82);" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 30, 0, 0, 10);"
        );

        // === CONTENT ===
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(80, 0, 100, 0));

        // -------- TITLE --------
        Label title = new Label("MineMates");

        Font arcade = Font.loadFont(
                getClass().getResourceAsStream("/fonts/ka1.ttf"),
                78
        );

        if (arcade != null) {
            title.setFont(arcade);
        } else {
            title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 52));
        }

        title.setTextFill(Color.web("#EAFBF4"));

        DropShadow glow = new DropShadow();
        glow.setRadius(18);
        glow.setSpread(0.25);
        glow.setColor(Color.web("#22C55E"));
        title.setEffect(glow);

        // -------- SUBTITLE --------
        Label subtitle = new Label("Two players • One goal • Shared victory");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#C7D2FE"));

        // Buttons styling
        stylePrimaryButton(startBtn);
        styleSecondaryButton(historyBtn);
        styleSecondaryButton(questionManagementBtn);

        // ✅ Updated: Use Main navigation helpers
        startBtn.setOnAction(e -> Main.showSetup(Main.getPrimaryStage()));
        historyBtn.setOnAction(e -> Main.showHistory(Main.getPrimaryStage()));
        questionManagementBtn.setOnAction(e -> Main.showQuestionManagement(Main.getPrimaryStage()));

        VBox btnContainer = new VBox(15);
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.getChildren().addAll(
                startBtn,
                historyBtn,
                questionManagementBtn
        );

        content.getChildren().addAll(title, subtitle, btnContainer);

        overlay.getChildren().add(content);
        this.getChildren().add(overlay);
    }

    // Neon green main button
    private void stylePrimaryButton(Button btn) {
        btn.setPrefSize(240, 48);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setStyle(
                "-fx-background-color: linear-gradient(#22C55E, #16A34A);" +
                "-fx-text-fill: #ECFDF5;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(34,197,94,0.45), 14, 0, 0, 6);"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-background-color: linear-gradient(#4ADE80, #22C55E);" +
                        "-fx-text-fill: #ECFDF5;" +
                        "-fx-background-radius: 14;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(34,197,94,0.75), 18, 0, 0, 8);"
                )
        );

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-background-color: linear-gradient(#22C55E, #16A34A);" +
                        "-fx-text-fill: #ECFDF5;" +
                        "-fx-background-radius: 14;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(34,197,94,0.45), 14, 0, 0, 6);"
                )
        );
    }

    // Dark glass secondary buttons + green hover
    private void styleSecondaryButton(Button btn) {
        btn.setPrefSize(240, 48);
        btn.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
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
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-cursor: hand;"
                )
        );

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-background-color: rgba(15,23,42,0.90);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-cursor: hand;"
                )
        );
    }
}
