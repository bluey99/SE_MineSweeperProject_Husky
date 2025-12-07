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

    // Container for difficulty description
    private VBox diffInfoBox;

    // Back button (like in HistoryView)
    public final Button backBtn = new Button("← Back to Menu");

    public SetupView(Main mainApp) {
        this.mainApp = mainApp;
        this.controller = new SetupController();
        buildUI();
    }

    private void buildUI() {

        // Background like menu
        this.setBackground(new Background(new BackgroundFill(
                Color.web("#0F0F1A"), CornerRadii.EMPTY, Insets.EMPTY)));

        // ---------- TOP BAR WITH BACK BUTTON ----------
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 20, 0, 20));

        styleBackButton(backBtn);
        backBtn.setOnAction(e -> Main.showMainMenu(Main.getPrimaryStage()));

        topBar.getChildren().add(backBtn);
        this.setTop(topBar);

        // ---------- MAIN CONTENT ----------
        VBox root = new VBox(18);                         // tighter spacing
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20, 40, 24, 40));      // smaller top/bottom padding
        root.setFillWidth(true);
        root.setPrefWidth(Double.MAX_VALUE);

        // Title
        Label title = new Label("Cooperative Minesweeper");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Two players • One goal • Shared victory");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#BBBBBB"));

        // Card container (dark style)
        VBox card = new VBox(16);               // smaller vertical spacing
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24));        // slightly smaller padding
        card.setPrefWidth(520);
        card.setMaxWidth(520);
        card.setStyle(
                "-fx-background-color: #1C1C2A;" +
                "-fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 15, 0, 0, 5);"
        );

        // Field labels + inputs
        Label p1Label = createLabel("Player 1 Name:");
        player1Field = createInput("Player 1");

        Label p2Label = createLabel("Player 2 Name:");
        player2Field = createInput("Player 2");

        // Difficulty section
        Label diffLabel = createLabel("Select Difficulty:");
        ToggleGroup diffGroup = new ToggleGroup();

        easyBtn   = createRadio("Easy", diffGroup);
        mediumBtn = createRadio("Medium", diffGroup);
        hardBtn   = createRadio("Hard", diffGroup);
        easyBtn.setSelected(true);

        HBox diffBox = new HBox(20, easyBtn, mediumBtn, hardBtn);
        diffBox.setAlignment(Pos.CENTER);

        // === Difficulty description as VBox of lines ===
        diffInfoBox = new VBox(2);
        diffInfoBox.setAlignment(Pos.CENTER);
        diffInfoBox.setPadding(new Insets(10, 0, 0, 0));
        diffInfoBox.setMaxWidth(350);

        updateDifficultyInfo("Easy"); // initial text

        // Update difficulty info dynamically
        easyBtn.setOnAction(e -> updateDifficultyInfo("Easy"));
        mediumBtn.setOnAction(e -> updateDifficultyInfo("Medium"));
        hardBtn.setOnAction(e -> updateDifficultyInfo("Hard"));

        card.getChildren().addAll(
                p1Label, player1Field,
                p2Label, player2Field,
                diffLabel, diffBox,
                diffInfoBox
        );

        // Wrap card to keep it centered
        HBox cardWrapper = new HBox(card);
        cardWrapper.setAlignment(Pos.CENTER);
        cardWrapper.setMaxWidth(Double.MAX_VALUE);
        cardWrapper.setPadding(new Insets(8, 0, 8, 0));

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

        // Add everything to root
        root.getChildren().addAll(title, subtitle, cardWrapper, startBtn);
        this.setCenter(root);
    }

    // === Build nice multi-line difficulty description ===
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
        }
    }

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

    // Helpers
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
        // Validation
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
