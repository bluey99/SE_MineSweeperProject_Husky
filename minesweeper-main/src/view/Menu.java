package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

        // === BACKGROUND IMAGE (blue + green diagonals) ===
        Image bg = new Image("img/menuBackground.jpg");

        BackgroundSize bgSize = new BackgroundSize(
                100, 100,           // width, height
                true, true,         // as % of region
                false, true         // contain=false, cover=true
        );

        BackgroundImage bgImage = new BackgroundImage(
                bg,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                bgSize
        );

        this.setBackground(new Background(bgImage));

        // === FULL-SCREEN OVERLAY PANEL (the "blue box") ===
        StackPane overlay = new StackPane();
        // make overlay always match the window size
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());

        overlay.setStyle(
                "-fx-background-color: rgba(3,10,25,0.82);" + // dark blue, transparent
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 30, 0, 0, 10);"
        );

        // === CONTENT INSIDE THE OVERLAY ===
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(80, 0, 100, 0)); // top/bottom padding so it breathes

        // Title
        Label title = new Label("Cooperative Minesweeper");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        title.setTextFill(Color.WHITE);

        // Subtitle
        Label subtitle = new Label("Two players • One goal • Shared victory");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#E5E7EB"));

        // Buttons styling
        stylePrimaryButton(startBtn);
        styleSecondaryButton(historyBtn);
        styleSecondaryButton(questionManagementBtn);

        VBox btnContainer = new VBox(15);
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.getChildren().addAll(
                startBtn,
                historyBtn,
                questionManagementBtn
        );

        content.getChildren().addAll(title, subtitle, btnContainer);

        // put content in the overlay, overlay in the root StackPane
        overlay.getChildren().add(content);
        this.getChildren().add(overlay);
    }

    // Bright blue main button
    private void stylePrimaryButton(Button btn) {
        btn.setPrefSize(220, 45);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setStyle(
                "-fx-background-color: linear-gradient(#4F46E5, #2563EB);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-background-color: linear-gradient(#60A5FA, #3B82F6);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
                )
        );

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-background-color: linear-gradient(#4F46E5, #2563EB);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
                )
        );
    }

    // Dark glass secondary buttons
    private void styleSecondaryButton(Button btn) {
        btn.setPrefSize(220, 45);
        btn.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        btn.setStyle(
                "-fx-background-color: rgba(15,23,42,0.90);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-background-color: rgba(30,64,175,0.95);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
                )
        );

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-background-color: rgba(15,23,42,0.90);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
                )
        );
    }
}
