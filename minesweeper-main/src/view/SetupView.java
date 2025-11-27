package view;

import controller.Main;
import controller.SetupController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SetupView extends BorderPane {

    private final Main mainApp;
    private final SetupController controller;

    private TextField player1Field;
    private TextField player2Field;
    private RadioButton easyBtn;
    private RadioButton mediumBtn;
    private RadioButton hardBtn;

    public SetupView(Main mainApp) {
        this.mainApp = mainApp;
        this.controller = new SetupController();
        buildUI();
    }

    private void buildUI() {

        this.setBackground(new Background(new BackgroundFill(
                Color.web("#E3F2FD"), CornerRadii.EMPTY, Insets.EMPTY)));

        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40));

        // Title
        Label title = new Label("Cooperative Minesweeper");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        title.setTextFill(Color.web("#1565C0"));

        Label subtitle = new Label("Two players • One goal • Shared score & lives");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#424242"));

        // Card
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));
        card.setMaxWidth(500);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
        );

        // Player 1
        Label p1Label = new Label("Player 1 Name:");
        p1Label.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        player1Field = new TextField("Player 1");
        player1Field.setPrefWidth(300);

        // Player 2
        Label p2Label = new Label("Player 2 Name:");
        p2Label.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        player2Field = new TextField("Player 2");
        player2Field.setPrefWidth(300);

        // Difficulty
        Label diffLabel = new Label("Select Difficulty:");
        diffLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        ToggleGroup diffGroup = new ToggleGroup();

        easyBtn = new RadioButton("Easy");
        mediumBtn = new RadioButton("Medium");
        hardBtn = new RadioButton("Hard");

        easyBtn.setToggleGroup(diffGroup);
        mediumBtn.setToggleGroup(diffGroup);
        hardBtn.setToggleGroup(diffGroup);
        easyBtn.setSelected(true);

        HBox diffBox = new HBox(20, easyBtn, mediumBtn, hardBtn);
        diffBox.setAlignment(Pos.CENTER);

        Label diffInfo = new Label(
                "Easy: 9×9 grid • 10 mines per board • 10 lives\n" +
                        "Medium: 13×13 grid • 26 mines per board • 8 lives\n" +
                        "Hard: 16×16 grid • 44 mines per board • 6 lives"
        );
        diffInfo.setFont(Font.font("Arial", 12));
        diffInfo.setTextFill(Color.web("#616161"));

        card.getChildren().addAll(
                p1Label, player1Field,
                p2Label, player2Field,
                diffLabel, diffBox,
                diffInfo
        );

        // Start button
        Button startBtn = new Button("▶ START GAME");
        startBtn.setPrefSize(250, 55);
        startBtn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        startBtn.setStyle(
                "-fx-background-color: #4CAF50;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;"
        );

        startBtn.setOnAction(e -> onStartPressed());

        root.getChildren().addAll(title, subtitle, card, startBtn);

        this.setCenter(root);
    }

    private void onStartPressed() {

        // Delegates validation to SetupController
        String validationError = controller.validatePlayerNames(
                player1Field.getText(),
                player2Field.getText()
        );

        if (validationError != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, validationError, ButtonType.OK);
            alert.setHeaderText("Invalid Player Names");
            alert.show();
            return;
        }

        String p1 = player1Field.getText().trim();
        String p2 = player2Field.getText().trim();
        String difficulty = easyBtn.isSelected() ? "Easy" :
                mediumBtn.isSelected() ? "Medium" : "Hard";

        // Pass values to Main
        mainApp.startGameFromSetup(p1, p2, difficulty);
    }
}
