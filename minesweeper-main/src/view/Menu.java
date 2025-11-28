package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Menu extends StackPane {

    public Button startBtn = new Button("Start Game");
    public Button historyBtn = new Button("History");
    public Button questionManagementBtn = new Button("Question Management"); // NEW BUTTON

    public Menu() {

        // Background color
        this.setStyle("-fx-background-color: #1e1e2f;"); // dark navy

        // Root container
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        // Title
        Label title = new Label("Cooperative Minesweeper");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        title.setTextFill(Color.WHITE);

        // Subtitle
        Label subtitle = new Label("Two players • One goal • Shared victory");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.LIGHTGRAY);

        // Style buttons
        stylePrimaryButton(startBtn);
        styleSecondaryButton(historyBtn);
        styleSecondaryButton(questionManagementBtn); // style new button same as secondary

        // Buttons container
        VBox btnContainer = new VBox(15);
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.getChildren().addAll(
                startBtn,
                historyBtn,
                questionManagementBtn // add new button to layout
        );

        // Add everything
        root.getChildren().addAll(title, subtitle, btnContainer);
        this.getChildren().add(root);
    }

    private void stylePrimaryButton(Button btn) {
        btn.setPrefSize(220, 45);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setStyle(
            "-fx-background-color: #3B82F6;" +  // blue
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
            btn.setStyle(
                "-fx-background-color: #2563EB;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            )
        );

        btn.setOnMouseExited(e ->
            btn.setStyle(
                "-fx-background-color: #3B82F6;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            )
        );
    }

    private void styleSecondaryButton(Button btn) {
        btn.setPrefSize(220, 45);
        btn.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        btn.setStyle(
            "-fx-background-color: #2a2a3a;" +  // dark gray
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
            btn.setStyle(
                "-fx-background-color: #3a3a4a;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            )
        );

        btn.setOnMouseExited(e ->
            btn.setStyle(
                "-fx-background-color: #2a2a3a;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            )
        );
    }
}
