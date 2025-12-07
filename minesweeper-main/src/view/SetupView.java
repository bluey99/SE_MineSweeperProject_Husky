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

<<<<<<< Updated upstream
    // NEW: container for difficulty description
    private VBox diffInfoBox;
=======
    // Back button (like in HistoryView)
    public final Button backBtn = new Button("← Back to Menu");
>>>>>>> Stashed changes

    public SetupView(Main mainApp) {
        this.mainApp = mainApp;
        this.controller = new SetupController();
        buildUI();
    }

    private void buildUI() {

        // Background
        this.setBackground(new Background(new BackgroundFill(
                Color.web("#0F0F1A"), CornerRadii.EMPTY, Insets.EMPTY)));

        // ---------- TOP BAR WITH BACK BUTTON ----------
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        // slightly smaller top padding than before
        topBar.setPadding(new Insets(10, 20, 0, 20));

        styleBackButton(backBtn);
        backBtn.setOnAction(e -> Main.showMainMenu(Main.getPrimaryStage()));

        topBar.getChildren().add(backBtn);
        this.setTop(topBar);

        // ---------- MAIN CONTENT ----------
        // smaller spacing so everything fits higher
        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        // smaller top/bottom padding (was 40)
        root.setPadding(new Insets(20, 40, 24, 40));
        root.setFillWidth(true);
        root.setPrefWidth(Double.MAX_VALUE);

        // Title
        Label title = new Label("Cooperative Minesweeper");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Two players • One goal • Shared victory");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#BBBBBB"));

        // Card container
        VBox card = new VBox(16);                 // was 20
        card.setAlignment(Pos.CENTER);
<<<<<<< Updated upstream
        card.setPadding(new Insets(30));
        card.setMaxWidth(500);
        VBox.setVgrow(card, Priority.ALWAYS);

=======
        card.setPadding(new Insets(24));          // was 30
        card.setPrefWidth(520);
        card.setMaxWidth(520);
>>>>>>> Stashed changes
        card.setStyle(
                "-fx-background-color: #1C1C2A;" +
                "-fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 15, 0, 0, 5);"
        );

        Label p1Label = createLabel("Player 1 Name:");
        player1Field = createInput("Player 1");

        Label p2Label = createLabel("Player 2 Name:");
        player2Field = createInput("Player 2");

        Label diffLabel = createLabel("Select Difficulty:");
        ToggleGroup diffGroup = new ToggleGroup();

        easyBtn   = createRadio("Easy", diffGroup);
        mediumBtn = createRadio("Medium", diffGroup);
        hardBtn   = createRadio("Hard", diffGroup);
        easyBtn.setSelected(true);

        HBox diffBox = new HBox(20, easyBtn, mediumBtn, hardBtn); // was 25
        diffBox.setAlignment(Pos.CENTER);

<<<<<<< Updated upstream
        // === NEW: Difficulty description as VBox of labels ===
        diffInfoBox = new VBox(2);
        diffInfoBox.setAlignment(Pos.CENTER);
        diffInfoBox.setPadding(new Insets(10, 0, 0, 0));
        diffInfoBox.setMaxWidth(350);

        updateDifficultyInfo("Easy"); // initial text

        // Update difficulty info dynamically
        easyBtn.setOnAction(e -> updateDifficultyInfo("Easy"));
        mediumBtn.setOnAction(e -> updateDifficultyInfo("Medium"));
        hardBtn.setOnAction(e -> updateDifficultyInfo("Hard"));
=======
        Label diffInfo = new Label(getDifficultyText("Easy"));
        diffInfo.setFont(Font.font("Arial", 12));
        diffInfo.setTextFill(Color.web("#A5A5A5"));
        diffInfo.setAlignment(Pos.CENTER);
        diffInfo.setWrapText(true);
        diffInfo.setPadding(new Insets(6, 0, 0, 0)); // smaller top padding

        easyBtn.setOnAction(e -> diffInfo.setText(getDifficultyText("Easy")));
        mediumBtn.setOnAction(e -> diffInfo.setText(getDifficultyText("Medium")));
        hardBtn.setOnAction(e -> diffInfo.setText(getDifficultyText("Hard")));
>>>>>>> Stashed changes

        card.getChildren().addAll(
                p1Label, player1Field,
                p2Label, player2Field,
                diffLabel, diffBox,
                diffInfoBox
        );

<<<<<<< Updated upstream
        // Start button (blue)
=======
        HBox cardWrapper = new HBox(card);
        cardWrapper.setAlignment(Pos.CENTER);
        cardWrapper.setMaxWidth(Double.MAX_VALUE);
        cardWrapper.setPadding(new Insets(8, 0, 8, 0));

>>>>>>> Stashed changes
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

        root.getChildren().addAll(title, subtitle, cardWrapper, startBtn);
        this.setCenter(root);
    }

<<<<<<< Updated upstream
    // === NEW: build nice multi-line description ===
    private void updateDifficultyInfo(String difficulty) {
        diffInfoBox.getChildren().clear();

        // Title line: "Easy Mode:" etc.
        Label title = new Label(difficulty + " Mode:");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        title.setTextFill(Color.web("#A5A5A5"));
        diffInfoBox.getChildren().add(title);

        String[] lines;
        switch (difficulty) {
            case "Easy":
                lines = new String[]{
                        "9×9 grid",
                        "10 mines",
                        "10 shared lives"
                };
                break;
            case "Medium":
                lines = new String[]{
                        "13×13 grid",
                        "26 mines",
                        "8 shared lives"
                };
                break;
            case "Hard":
                lines = new String[]{
                        "16×16 grid",
                        "44 mines",
                        "6 shared lives"
                };
                break;
            default:
                lines = new String[0];
        }

        for (String line : lines) {
            Label l = new Label("• " + line);
            l.setFont(Font.font("Arial", 12));
            l.setTextFill(Color.web("#A5A5A5"));
            diffInfoBox.getChildren().add(l);
=======
    // STYLE back button like in HistoryView
    private void styleBackButton(Button btn) {
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setBackground(Background.EMPTY);
        btn.setPadding(new Insets(4, 8, 4, 0));
        btn.setStyle(
                "-fx-text-fill: #BFDBFE;" +
                "-fx-cursor: hand;" +
                "-fx-background-color: transparent;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-text-fill: #FFFFFF;" +
                        "-fx-underline: true;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-color: transparent;"
                ));

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-text-fill: #BFDBFE;" +
                        "-fx-underline: false;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-color: transparent;"
                ));
    }

    private String getDifficultyText(String difficulty) {
        switch (difficulty) {
            case "Easy":
                return "Easy Mode:\n• 9×9 grid\n• 10 mines\n• 10 shared lives";
            case "Medium":
                return "Medium Mode:\n• 13×13 grid\n• 26 mines\n• 8 shared lives";
            case "Hard":
                return "Hard Mode:\n• 16×16 grid\n• 44 mines\n• 6 shared lives";
>>>>>>> Stashed changes
        }
        return "";
    }

<<<<<<< Updated upstream
    // Helpers
=======
>>>>>>> Stashed changes
    private Label createLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lbl.setTextFill(Color.WHITE);
        return lbl;
    }

    private TextField createInput(String placeholder) {
        TextField field = new TextField(placeholder);
        field.setPrefWidth(420);
        field.setStyle(
                "-fx-background-color: #2D2D3F;" +
                "-fx-text-fill: white;" +
                "-fx-border-color: #444;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;"
        );
        return field;
    }

    private RadioButton createRadio(String text, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group);
        rb.setFont(Font.font("Arial", 13));
        rb.setTextFill(Color.WHITE);
        rb.setStyle("-fx-cursor: hand;");
        return rb;
    }

    private void onStartPressed() {
<<<<<<< Updated upstream

        // Validation
=======
>>>>>>> Stashed changes
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

        mainApp.startGameFromSetup(p1, p2, difficulty);
    }
}
