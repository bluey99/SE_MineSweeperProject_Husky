package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Question;

/**
 * View component for managing trivia questions.
 * 
 * Responsibilities:
 * - Define and style UI components
 * - Present question data in a table
 * - Reflect UI state (enabled/disabled buttons)
 *
 * Note:
 * - No business logic is handled here.
 * - All behavior (event handling, state changes) is managed by the Controller.
 */
public class QuestionManagementView extends BorderPane {

    // Top-level navigation and action buttons (public for controller access)
    public final Button backBtn = new Button("Menu");
    public final Button addBtn = new Button("+ Add Question");
    public final Button editBtn = new Button("Edit Selected");
    public final Button deleteBtn = new Button("Delete Selected");

    /**
     * Styles for secondary action buttons.
     * 
     * Enabled / disabled styles are separated so that the visual state
     * clearly reflects whether an action is currently available.
     */
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

    // Table displaying all questions (data injected by the controller)
    public final TableView<Question> table = new TableView<>();

    public QuestionManagementView() {

        // Global background color for the screen
        setStyle("-fx-background-color: #0f172a;");

        // ---------------------------------------------------------------------
        // Top bar (navigation + title)
        // ---------------------------------------------------------------------
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(20, 30, 10, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Back button styling (consistent with other views)
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

        /**
         * Initial UI state:
         * Edit and Delete are disabled until a table row is selected.
         * 
         * The controller is responsible for enabling them when appropriate.
         */
        editBtn.setDisable(true);
        deleteBtn.setDisable(true);

        // Title and subtitle
        Label iconLabel = new Label("📚");
        iconLabel.setTextFill(Color.web("#8B5CF6"));
        iconLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        VBox titleBox = new VBox(5);
        Label title = new Label("Question Management");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));

        Label subtitle = new Label("Manage trivia questions for the game");
        subtitle.setTextFill(Color.web("#9CA3AF"));
        subtitle.setFont(Font.font("Arial", 14));

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        stylePrimary(addBtn);

        topBar.getChildren().addAll(backBtn, iconLabel, titleBox, spacer);
        setTop(topBar);

        // ---------------------------------------------------------------------
        // Center table (question list)
        // ---------------------------------------------------------------------
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Placeholder shown when no questions exist
        Label placeholder = new Label("No questions yet. Click \"Add Question\" to create one.");
        placeholder.setTextFill(Color.web("#9CA3AF"));
        placeholder.setFont(Font.font("Arial", 14));
        table.setPlaceholder(placeholder);

        // Table visual styling
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

        // ---------------------------------------------------------------------
        // Table columns
        // ---------------------------------------------------------------------
        TableColumn<Question, String> qCol = new TableColumn<>("Question");
        qCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getText())
        );

        TableColumn<Question, String> diffCol = new TableColumn<>("Difficulty");
        diffCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDifficulty().toString()
            )
        );

        /**
         * Custom cell rendering for difficulty:
         * Displays difficulty as a colored pill-style label
         * to improve visual scanning and readability.
         */
        diffCol.setCellFactory(col -> new TableCell<>() {
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
                }
            }
        });

        TableColumn<Question, String> correctCol = new TableColumn<>("Correct Answer");
        correctCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCorrectAnswerText()
            )
        );

        table.getColumns().addAll(qCol, diffCol, correctCol);

        VBox centerBox = new VBox(10, table);
        centerBox.setPadding(new Insets(10, 30, 10, 30));
        setCenter(centerBox);

        // ---------------------------------------------------------------------
        // Bottom action buttons
        // ---------------------------------------------------------------------
        HBox bottom = new HBox(10);
        bottom.setPadding(new Insets(10, 30, 20, 30));
        bottom.setAlignment(Pos.CENTER_RIGHT);

        // Apply consistent styling to action buttons
        styleSecondary(addBtn);
        styleSecondary(editBtn);
        styleSecondary(deleteBtn);

        // Fixed width for visual alignment
        addBtn.setPrefWidth(140);
        editBtn.setPrefWidth(140);
        deleteBtn.setPrefWidth(140);

        /**
         * Wrappers are used to enable tooltips on disabled buttons.
         * JavaFX does not show tooltips on disabled nodes directly.
         */
        HBox editWrapper = new HBox(editBtn);
        HBox deleteWrapper = new HBox(deleteBtn);

        Tooltip editTip = new Tooltip("Select a question from the table to edit");
        Tooltip deleteTip = new Tooltip("Select a question from the table to delete");

        Tooltip.install(editWrapper, editTip);
        Tooltip.install(deleteWrapper, deleteTip);

        bottom.getChildren().addAll(addBtn, editWrapper, deleteWrapper);
        setBottom(bottom);
    }

    /**
     * Styles the primary action button (Add Question).
     */
    private void stylePrimary(Button btn) {
        btn.setPrefHeight(36);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setStyle(
            "-fx-background-color: #2563EB;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 18;" +
            "-fx-padding: 0 18 0 18;" +
            "-fx-cursor: hand;"
        );
    }

    /**
     * Styles secondary buttons and automatically updates their
     * appearance when their disabled state changes.
     */
    private void styleSecondary(Button btn) {
        btn.setPrefHeight(32);
        btn.setFont(Font.font("Arial", 13));

        // Apply correct initial style
        btn.setStyle(btn.isDisabled()
                ? SECONDARY_DISABLED_STYLE
                : SECONDARY_ENABLED_STYLE);

        // React to enabled / disabled state changes
        btn.disabledProperty().addListener((obs, wasDisabled, isNowDisabled) -> {
            btn.setStyle(isNowDisabled
                    ? SECONDARY_DISABLED_STYLE
                    : SECONDARY_ENABLED_STYLE);
        });
    }
}
