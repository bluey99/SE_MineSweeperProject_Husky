package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.GameHistoryEntry;

public class HistoryView extends BorderPane {

    // Top navigation
    public final Button backBtn = new Button("Menu");

    // Actions
    public final Button clearHistoryBtn = new Button("Clear History");
    public final Button trimHistoryBtn = new Button("Trim History");
    public final Button deleteSelectedBtn = new Button("Delete Selected");

    // Status / empty
    public final Label statusLabel = new Label("");
    public final Label emptyLabel = new Label("No history yet.");

    // Table
    public final TableView<GameHistoryEntry> table = new TableView<>();

    // Same style idea as QuestionManagementView
    private static final String SECONDARY_ENABLED_STYLE =
            "-fx-background-color: #1F2937;" +
            "-fx-text-fill: #E5E7EB;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 0 14 0 14;" +
            "-fx-cursor: hand;";

    private static final String SECONDARY_DISABLED_STYLE =
            "-fx-background-color: #111827;" +
            "-fx-text-fill: #6B7280;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 0 14 0 14;" +
            "-fx-cursor: default;" +
            "-fx-opacity: 0.6;";

    private static final String DANGER_ENABLED_STYLE =
            "-fx-background-color: #DC2626;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 0 14 0 14;" +
            "-fx-cursor: hand;";

    private static final String DANGER_DISABLED_STYLE =
            "-fx-background-color: #7F1D1D;" +
            "-fx-text-fill: rgba(255,255,255,0.55);" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 0 14 0 14;" +
            "-fx-cursor: default;" +
            "-fx-opacity: 0.6;";

    public HistoryView() {
        buildUI();
    }

    private void buildUI() {

        // Global background
        setStyle("-fx-background-color: #0f172a;");

        // ---------------------------------------------------------------------
        // Top bar (same layout vibe as QuestionManagementView)
        // ---------------------------------------------------------------------
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(20, 30, 10, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);

        backBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        String normalStyle = """
            -fx-background-color: #1e293b;
            -fx-text-fill: #e5e7eb;
            -fx-background-radius: 999;
            -fx-padding: 7 18 7 18;
            -fx-cursor: hand;
        """;

        String hoverStyle = """
            -fx-background-color: #334155;
            -fx-text-fill: #ffffff;
            -fx-background-radius: 999;
            -fx-padding: 7 18 7 18;
            -fx-cursor: hand;
        """;

        backBtn.setStyle(normalStyle);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(hoverStyle));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(normalStyle));

        Label iconLabel = new Label("🕒");
        iconLabel.setTextFill(Color.web("#8B5CF6"));
        iconLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        VBox titleBox = new VBox(5);
        Label title = new Label("Game History");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));

        Label subtitle = new Label("Review previous cooperative games");
        subtitle.setTextFill(Color.web("#9CA3AF"));
        subtitle.setFont(Font.font("Arial", 14));

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, iconLabel, titleBox, spacer);
        setTop(topBar);

        // ---------------------------------------------------------------------
        // Actions row (clean like Question Management)
        // ---------------------------------------------------------------------
        clearHistoryBtn.setPrefHeight(32);
        trimHistoryBtn.setPrefHeight(32);
        deleteSelectedBtn.setPrefHeight(32);

        clearHistoryBtn.setFont(Font.font("Arial", 13));
        trimHistoryBtn.setFont(Font.font("Arial", 13));
        deleteSelectedBtn.setFont(Font.font("Arial", 13));

        styleSecondary(clearHistoryBtn);
        styleSecondary(trimHistoryBtn);
        styleDanger(deleteSelectedBtn);

        // (Optional) Disable delete until row selected (controller can manage too)
        deleteSelectedBtn.setDisable(true);

        HBox actions = new HBox(10, clearHistoryBtn, trimHistoryBtn, deleteSelectedBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        statusLabel.setTextFill(Color.web("#93C5FD"));
        statusLabel.setFont(Font.font("Arial", 13));

        // ---------------------------------------------------------------------
        // Table styling — copied from QuestionManagementView
        // ---------------------------------------------------------------------
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label placeholder = new Label("No history yet.");
        placeholder.setTextFill(Color.web("#9CA3AF"));
        placeholder.setFont(Font.font("Arial", 14));
        table.setPlaceholder(placeholder);

        table.setStyle(
            "-fx-background-color: #020617;" +
            "-fx-control-inner-background: #020617;" +
            "-fx-border-color: #1E293B;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-accent: #2563EB;" +
            "-fx-selection-bar: #2563EB;" +
            "-fx-selection-bar-non-focused: #2563EB;" +
            "-fx-selection-bar-text: white;" +
            "-fx-cell-hover-color: #1E293B;" +
            "-fx-table-cell-border-color: transparent;" +
            "-fx-table-header-border-color: #1E293B;"
        );

        // Columns
        TableColumn<GameHistoryEntry, String> cDate = new TableColumn<>("Date & Time");
        cDate.setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        TableColumn<GameHistoryEntry, String> cDiff = new TableColumn<>("Difficulty");
        cDiff.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        cDiff.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label tag = new Label(item);
                    tag.setPadding(new Insets(4, 10, 4, 10));
                    tag.setTextFill(Color.WHITE);
                    tag.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                    String color;
                    switch (item) {
                        case "Medium": color = "#D97706"; break;
                        case "Hard":   color = "#DC2626"; break;
                        case "Expert": color = "#7C3AED"; break;
                        default:       color = "#059669"; // Easy
                    }
                    tag.setStyle("-fx-background-radius: 999; -fx-background-color: " + color + ";");
                    setGraphic(tag);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<GameHistoryEntry, String> cP1 = new TableColumn<>("Player 1");
        cP1.setCellValueFactory(new PropertyValueFactory<>("player1Name"));

        TableColumn<GameHistoryEntry, String> cP2 = new TableColumn<>("Player 2");
        cP2.setCellValueFactory(new PropertyValueFactory<>("player2Name"));

        TableColumn<GameHistoryEntry, String> cRes = new TableColumn<>("Result");
        cRes.setCellValueFactory(new PropertyValueFactory<>("result"));
        cRes.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }

                Label pill = new Label(item.toUpperCase());
                pill.setPadding(new Insets(4, 10, 4, 10));
                pill.setTextFill(Color.WHITE);
                pill.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                String bg = "WIN".equalsIgnoreCase(item) ? "#16A34A" : "#DC2626";
                pill.setStyle("-fx-background-radius: 999; -fx-background-color: " + bg + ";");

                setGraphic(pill);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<GameHistoryEntry, Integer> cScore = new TableColumn<>("Final Score");
        cScore.setCellValueFactory(new PropertyValueFactory<>("finalScore"));

        TableColumn<GameHistoryEntry, Integer> cTime = new TableColumn<>("Game Length (sec)");
        cTime.setCellValueFactory(new PropertyValueFactory<>("gameLengthSeconds"));

        table.getColumns().setAll(cDate, cDiff, cP1, cP2, cRes, cScore, cTime);

        // Optional empty label (kept from your version)
        emptyLabel.setTextFill(Color.web("#9CA3AF"));
        emptyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        // Enable/disable Delete Selected automatically based on selection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            deleteSelectedBtn.setDisable(newV == null);
        });

        // ---------------------------------------------------------------------
        // Center layout
        // ---------------------------------------------------------------------
        VBox centerBox = new VBox(10, actions, statusLabel, table, emptyLabel);
        centerBox.setPadding(new Insets(10, 30, 20, 30));
        setCenter(centerBox);
    }

    // ----------------------- Public helper: popup for trimming -----------------------
    // Call this from your controller when trimHistoryBtn is clicked.
    // Returns: K (>0) or -1 if cancelled/invalid.
    public int showTrimHistoryDialog() {

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Trim History");
        dialog.setHeaderText(null);

        ButtonType trimType = new ButtonType("Trim", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, trimType);

        Label title = new Label("Trim History");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label desc = new Label("Keep only the most recent:");
        desc.setStyle("-fx-text-fill: #9CA3AF;");

        ToggleGroup group = new ToggleGroup();
        RadioButton r5 = mkRadio("5", group);
        RadioButton r10 = mkRadio("10", group);
        RadioButton r20 = mkRadio("20", group);
        RadioButton r50 = mkRadio("50", group);
        RadioButton rCustom = mkRadio("Custom", group);

        r10.setSelected(true);

        TextField customField = new TextField();
        customField.setPromptText("Enter K");
        customField.setPrefWidth(120);
        customField.setDisable(true);
        customField.setStyle(
                "-fx-background-color: #020617;" +
                "-fx-text-fill: #E5E7EB;" +
                "-fx-prompt-text-fill: #6B7280;" +
                "-fx-border-color: #1E293B;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 6 10 6 10;"
        );

        rCustom.selectedProperty().addListener((obs, was, isNow) -> {
            customField.setDisable(!isNow);
            if (isNow) customField.requestFocus();
            if (!isNow) customField.clear();
        });

        // Only digits in custom field
        customField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            String filtered = newV.replaceAll("[^0-9]", "");
            if (!filtered.equals(newV)) customField.setText(filtered);
        });

        VBox radios = new VBox(8,
                r5, r10, r20, r50,
                new HBox(10, rCustom, customField)
        );
        radios.setPadding(new Insets(10, 0, 0, 0));
        ((HBox) radios.getChildren().get(radios.getChildren().size() - 1)).setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(10, title, desc, radios);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #0f172a;");

        dialog.getDialogPane().setContent(root);

        // Dark dialog pane
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #0f172a;" +
                "-fx-border-color: #1E293B;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        // Style dialog buttons
        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        Button trimBtn = (Button) dialog.getDialogPane().lookupButton(trimType);

        cancelBtn.setStyle(
                "-fx-background-color: #1F2937;" +
                "-fx-text-fill: #E5E7EB;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 6 14 6 14;"
        );

        trimBtn.setStyle(
                "-fx-background-color: #2563EB;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 6 14 6 14;"
        );

        // Disable Trim if Custom selected but empty
        trimBtn.disableProperty().bind(
                rCustom.selectedProperty().and(customField.textProperty().isEmpty())
        );

        dialog.setResultConverter(bt -> {
            if (bt != trimType) return null;

            Toggle selected = group.getSelectedToggle();
            if (!(selected instanceof RadioButton rb)) return null;

            String txt = rb.getText();
            if (!"Custom".equals(txt)) return Integer.parseInt(txt);

            String raw = customField.getText();
            if (raw == null || raw.isBlank()) return null;

            int k = Integer.parseInt(raw.trim());
            return (k > 0) ? k : null;
        });

        return dialog.showAndWait().orElse(-1);
    }

    private RadioButton mkRadio(String text, ToggleGroup group) {
        RadioButton r = new RadioButton(text);
        r.setToggleGroup(group);
        r.setStyle("-fx-text-fill: #E5E7EB;");
        return r;
    }

    // ----------------------- Styling helpers -----------------------
    private void styleSecondary(Button btn) {
        btn.setPrefHeight(32);
        btn.setFont(Font.font("Arial", 13));
        btn.setStyle(btn.isDisabled() ? SECONDARY_DISABLED_STYLE : SECONDARY_ENABLED_STYLE);
        btn.disabledProperty().addListener((obs, wasDisabled, isNowDisabled) -> {
            btn.setStyle(isNowDisabled ? SECONDARY_DISABLED_STYLE : SECONDARY_ENABLED_STYLE);
        });
    }

    private void styleDanger(Button btn) {
        btn.setPrefHeight(32);
        btn.setFont(Font.font("Arial", 13));
        btn.setStyle(btn.isDisabled() ? DANGER_DISABLED_STYLE : DANGER_ENABLED_STYLE);
        btn.disabledProperty().addListener((obs, wasDisabled, isNowDisabled) -> {
            btn.setStyle(isNowDisabled ? DANGER_DISABLED_STYLE : DANGER_ENABLED_STYLE);
        });
    }
}
