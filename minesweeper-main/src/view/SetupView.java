package view;

import controller.Main;
import controller.SetupController;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
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

    private VBox diffInfoBox;
    private HBox diffBox;

    // SAME Menu button (unchanged)
    public final Button backBtn = new Button("Menu");

    public SetupView(Main mainApp) {
        this.mainApp = mainApp;
        this.controller = new SetupController();
        buildUI();
    }

    private void buildUI() {

        // ===== Background (space / game-style) =====
        RadialGradient bg = new RadialGradient(
                0, 0,
                0.5, 0.2,
                0.8,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0E1A33")),
                new Stop(0.45, Color.web("#0B1020")),
                new Stop(1, Color.web("#070A14"))
        );
        setBackground(new Background(new BackgroundFill(bg, CornerRadii.EMPTY, Insets.EMPTY)));

        // ===== TOP BAR =====
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(18, 28, 8, 28));
        topBar.setAlignment(Pos.CENTER_LEFT);

        styleMenuButton(backBtn);
        backBtn.setOnAction(e -> Main.showMainMenu(Main.getPrimaryStage()));

        topBar.getChildren().add(backBtn);
        setTop(topBar);

        // ===== CENTER CONTENT =====
        VBox root = new VBox(14);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(10, 30, 20, 30));

        Label title = new Label("Cooperative Minesweeper");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 34));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Two players • One goal • Shared victory");
        subtitle.setFont(Font.font("Arial", 15));
        subtitle.setTextFill(Color.web("#9CA3AF"));

        // Accent line (blue -> green)
        Region accent = new Region();
        accent.setPrefHeight(4);
        accent.setMaxWidth(260);
        accent.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#2563EB")),
                        new Stop(1, Color.web("#22C55E"))
                ),
                new CornerRadii(999),
                Insets.EMPTY
        )));

        VBox heading = new VBox(6, title, subtitle, accent);
        heading.setAlignment(Pos.CENTER);

        // ===== FORM CARD =====
        VBox form = new VBox(12);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setMaxWidth(620);

        // Responsive width (no scrolling)
        form.prefWidthProperty().bind(
                Bindings.min(620,
                        Bindings.max(420, widthProperty().multiply(0.6)))
        );

        form.setBackground(new Background(new BackgroundFill(
                Color.web("rgba(255,255,255,0.06)"),
                new CornerRadii(18),
                Insets.EMPTY
        )));
        form.setBorder(new Border(new BorderStroke(
                Color.web("rgba(37,99,235,0.25)"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(18),
                new BorderWidths(1.2)
        )));
        form.setEffect(new DropShadow(22, Color.rgb(0, 0, 0, 0.45)));

        VBox p1Wrap = createLabeledInput("Player 1 Name", "Player 1");
        player1Field = (TextField) p1Wrap.getChildren().get(1);

        VBox p2Wrap = createLabeledInput("Player 2 Name", "Player 2");
        player2Field = (TextField) p2Wrap.getChildren().get(1);

        Label diffLabel = new Label("Select Difficulty");
        diffLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        diffLabel.setTextFill(Color.web("#E5E7EB"));

        ToggleGroup group = new ToggleGroup();
        easyBtn = createRadio("Easy", group);
        mediumBtn = createRadio("Medium", group);
        hardBtn = createRadio("Hard", group);
        easyBtn.setSelected(true);

        diffBox = new HBox(24, easyBtn, mediumBtn, hardBtn);
        diffBox.setAlignment(Pos.CENTER);
        diffBox.setPadding(new Insets(10));
        diffBox.setBackground(new Background(new BackgroundFill(
                Color.web("rgba(2,6,23,0.45)"),
                new CornerRadii(14),
                Insets.EMPTY
        )));

        diffInfoBox = new VBox(3);
        diffInfoBox.setAlignment(Pos.CENTER);
        diffInfoBox.setPadding(new Insets(10));
        diffInfoBox.setBackground(new Background(new BackgroundFill(
                Color.web("rgba(2,6,23,0.6)"),
                new CornerRadii(14),
                Insets.EMPTY
        )));

        // Initial info + green accent
        updateDifficultyInfo("Easy");

        // Update on click
        easyBtn.setOnAction(e -> updateDifficultyInfo("Easy"));
        mediumBtn.setOnAction(e -> updateDifficultyInfo("Medium"));
        hardBtn.setOnAction(e -> updateDifficultyInfo("Hard"));

        form.getChildren().addAll(p1Wrap, p2Wrap, diffLabel, diffBox, diffInfoBox);

        // ===== START BUTTON =====
        Button startBtn = new Button("Start Game");
        startBtn.setPrefHeight(50);
        startBtn.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 17));

        startBtn.prefWidthProperty().bind(
                Bindings.min(340,
                        Bindings.max(240, form.prefWidthProperty().multiply(0.55)))
        );

        String normal = """
            -fx-background-color: #2563EB;
            -fx-text-fill: white;
            -fx-background-radius: 14;
            -fx-cursor: hand;
        """;

        // Hover: blue -> green gradient
        String hover = """
            -fx-background-color: linear-gradient(to right, #1D4ED8, #22C55E);
            -fx-text-fill: white;
            -fx-background-radius: 14;
            -fx-cursor: hand;
        """;

        startBtn.setStyle(normal);
        startBtn.setEffect(new DropShadow(16, Color.rgb(0, 0, 0, 0.35)));
        startBtn.setOnMouseEntered(e -> startBtn.setStyle(hover));
        startBtn.setOnMouseExited(e -> startBtn.setStyle(normal));
        startBtn.setOnAction(e -> onStartPressed());

        root.getChildren().addAll(heading, form, startBtn);
        setCenter(root);
    }

    // ===== Difficulty Info (adds green/blue/red border) =====
    private void updateDifficultyInfo(String difficulty) {
        diffInfoBox.getChildren().clear();

        String borderColor = switch (difficulty) {
            case "Easy" -> "#22C55E";   // green
            case "Medium" -> "#3B82F6"; // blue
            case "Hard" -> "#EF4444";   // red
            default -> "#334155";
        };

        diffInfoBox.setBorder(new Border(new BorderStroke(
                Color.web(borderColor, 0.6),
                BorderStrokeStyle.SOLID,
                new CornerRadii(14),
                new BorderWidths(1.5)
        )));

        Label title = new Label(difficulty + " Mode:");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        title.setTextFill(Color.web("#BFDBFE"));
        diffInfoBox.getChildren().add(title);

        String[] lines = switch (difficulty) {
            case "Medium" -> new String[]{"13×13 grid", "26 mines", "8 shared lives"};
            case "Hard" -> new String[]{"16×16 grid", "44 mines", "6 shared lives"};
            default -> new String[]{"9×9 grid", "10 mines", "10 shared lives"};
        };

        for (String l : lines) {
            Label line = new Label("• " + l);
            line.setFont(Font.font("Arial", 12));
            line.setTextFill(Color.web("#94A3B8"));
            diffInfoBox.getChildren().add(line);
        }
    }

    // ===== Helpers =====
    private VBox createLabeledInput(String label, String placeholder) {
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#9CA3AF"));

        TextField field = new TextField();
        field.setPromptText(placeholder);
        field.setFont(Font.font("Arial", 14));
        field.setStyle("""
            -fx-background-color: rgba(255,255,255,0.08);
            -fx-text-fill: #E5E7EB;
            -fx-prompt-text-fill: rgba(148,163,184,0.8);
            -fx-background-radius: 14;
            -fx-border-radius: 14;
            -fx-border-color: rgba(148,163,184,0.18);
            -fx-padding: 11 14;
        """);

        return new VBox(6, lbl, field);
    }

    private RadioButton createRadio(String text, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group);
        rb.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rb.setTextFill(Color.web("#E5E7EB"));
        rb.setStyle("-fx-cursor: hand;");

        // Add green (Easy), blue (Medium), red (Hard)
        switch (text) {
            case "Easy" -> rb.setStyle(rb.getStyle() + "-fx-mark-color: #22C55E;");
            case "Medium" -> rb.setStyle(rb.getStyle() + "-fx-mark-color: #3B82F6;");
            case "Hard" -> rb.setStyle(rb.getStyle() + "-fx-mark-color: #EF4444;");
        }
        return rb;
    }

    // Menu button style (unchanged)
    private void styleMenuButton(Button btn) {
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        String normalStyle = """
            -fx-background-color: #1e293b;
            -fx-text-fill: #e5e7eb;
            -fx-background-radius: 999;
            -fx-padding: 7 18;
            -fx-cursor: hand;
        """;

        String hoverStyle = """
            -fx-background-color: #334155;
            -fx-text-fill: #ffffff;
            -fx-background-radius: 999;
            -fx-padding: 7 18;
            -fx-cursor: hand;
        """;

        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
    }

    // Start logic (keep Player1 / Player2 defaults)
    private void onStartPressed() {
        String p1 = player1Field.getText() == null ? "" : player1Field.getText().trim();
        String p2 = player2Field.getText() == null ? "" : player2Field.getText().trim();

        if (p1.isEmpty()) p1 = "Player 1";
        if (p2.isEmpty()) p2 = "Player 2";

        String diff = easyBtn.isSelected() ? "Easy" :
                mediumBtn.isSelected() ? "Medium" : "Hard";

        mainApp.startGameFromSetup(p1, p2, diff);
    }
}
