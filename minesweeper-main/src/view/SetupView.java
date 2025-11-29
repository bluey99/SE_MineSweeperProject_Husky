package view;

import controller.Main;
import controller.SetupController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

        // Dark background like menu
        this.setBackground(new Background(new BackgroundFill(
                Color.web("#0F0F1A"), CornerRadii.EMPTY, Insets.EMPTY)));

        VBox root = new VBox(25);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(60));

        // Title
        Label title = new Label("Cooperative Minesweeper");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Two players • One goal • Shared victory");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#BBBBBB"));

        // Card container (dark style)
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(500);
        card.setStyle(
                "-fx-background-color: #1C1C2A;" +
                "-fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 15, 0, 0, 5);"
        );

        // Field labels
        Label p1Label = createLabel("Player 1 Name:");
        player1Field = createInput("Player 1");

        Label p2Label = createLabel("Player 2 Name:");
        player2Field = createInput("Player 2");

        // Difficulty section
        Label diffLabel = createLabel("Select Difficulty:");

        ToggleGroup diffGroup = new ToggleGroup();

        easyBtn = createRadio("Easy", diffGroup);
        mediumBtn = createRadio("Medium", diffGroup);
        hardBtn = createRadio("Hard", diffGroup);
        easyBtn.setSelected(true);

        HBox diffBox = new HBox(25, easyBtn, mediumBtn, hardBtn);
        diffBox.setAlignment(Pos.CENTER);

        Label diffInfo = new Label(
                "Easy: 9×9 grid • 10 mines • 10 lives\n" +
                "Medium: 13×13 grid • 26 mines • 8 lives\n" +
                "Hard: 16×16 grid • 44 mines • 6 lives"
        );
        diffInfo.setFont(Font.font("Arial", 12));
        diffInfo.setTextFill(Color.web("#A5A5A5"));
        diffInfo.setAlignment(Pos.CENTER);
        diffInfo.setWrapText(true);

        card.getChildren().addAll(
                p1Label, player1Field,
                p2Label, player2Field,
                diffLabel, diffBox,
                diffInfo
        );

        // Start button (blue like menu)
        Button startBtn = new Button("Start Game");
        startBtn.setPrefSize(250, 55);
        startBtn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        startBtn.setStyle(
                "-fx-background-color: #3F7CFF;" +
                "-fx-background-radius: 10;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
        );

        startBtn.setOnMouseEntered(e ->
                startBtn.setStyle(
                        "-fx-background-color: #2F6BF5;" +
                        "-fx-background-radius: 10;" +
                        "-fx-text-fill: white;"
                )
        );

        startBtn.setOnMouseExited(e ->
                startBtn.setStyle(
                        "-fx-background-color: #3F7CFF;" +
                        "-fx-background-radius: 10;" +
                        "-fx-text-fill: white;"
                )
        );

        startBtn.setOnAction(e -> onStartPressed());

        root.getChildren().addAll(title, subtitle, card, startBtn);
        this.setCenter(root);
    }

    // Helper for labels
    private Label createLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lbl.setTextFill(Color.WHITE);
        return lbl;
    }

    // Helper for text fields
    private TextField createInput(String placeholder) {
        TextField field = new TextField(placeholder);
        field.setPrefWidth(300);
        field.setStyle(
                "-fx-background-color: #2D2D3F;" +
                "-fx-text-fill: white;" +
                "-fx-border-color: #444;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;"
        );
        return field;
    }

    // Helper for radio buttons
    private RadioButton createRadio(String text, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group);
        rb.setFont(Font.font("Arial", 13));
        rb.setTextFill(Color.WHITE);
        rb.setStyle("-fx-cursor: hand;");
        return rb;
    }

    private void onStartPressed() {

        // Validation via SetupController
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

        // Pass info to Main
        mainApp.startGameFromSetup(p1, p2, difficulty);
    }
}
