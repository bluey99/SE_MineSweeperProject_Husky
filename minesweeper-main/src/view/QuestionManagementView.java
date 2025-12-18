package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Question;

public class QuestionManagementView extends BorderPane {

    public final Button backBtn = new Button("← Back to Menu");
    public final Button addBtn = new Button("+ Add Question");
    public final Button editBtn = new Button("Edit Selected");
    public final Button deleteBtn = new Button("Delete Selected");

    public final TableView<Question> table = new TableView<>();

    public QuestionManagementView() {
        setStyle("-fx-background-color: #0f172a;"); // dark background

        // ---------------------------------------------------------------------
        // Top bar
        // ---------------------------------------------------------------------
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(20, 30, 10, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);

        backBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #60A5FA;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;"
        );

        Label iconLabel = new Label("\uD83D\uDCDA"); // 📚
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

        topBar.getChildren().addAll(backBtn, iconLabel, titleBox, spacer, addBtn);
        setTop(topBar);

        // ---------------------------------------------------------------------
        // Center table
        // ---------------------------------------------------------------------
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label placeholder = new Label("No questions yet. Click \"Add Question\" to create one.");
        placeholder.setTextFill(Color.web("#9CA3AF"));
        placeholder.setFont(Font.font("Arial", 14));
        table.setPlaceholder(placeholder);

        // ✅ ONLY TABLE COLORS (no rowFactory — selection will work properly)
        table.setStyle(
            // background + borders
            "-fx-background-color: #020617;" +
            "-fx-control-inner-background: #020617;" +
            "-fx-border-color: #1E293B;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +

            // selection colors (THIS makes selected row visible)
            "-fx-accent: #2563EB;" +                    // main selection color
            "-fx-selection-bar: #2563EB;" +
            "-fx-selection-bar-non-focused: #2563EB;" +
            "-fx-selection-bar-text: white;" +          // selected text color

            // hover color
            "-fx-cell-hover-color: #1E293B;" +

            // reduce gridlines
            "-fx-table-cell-border-color: transparent;" +
            "-fx-table-header-border-color: #1E293B;"
        );

        // ---------------------------------------------------------------------
        // Columns
        // ---------------------------------------------------------------------
        TableColumn<Question, String> qCol = new TableColumn<>("Question");
        qCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getText()
        ));

        TableColumn<Question, String> diffCol = new TableColumn<>("Difficulty");
        diffCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDifficulty().toString()
        ));

        diffCol.setCellFactory(col -> new TableCell<Question, String>() {
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
                }
            }
        });

        TableColumn<Question, String> correctCol = new TableColumn<>("Correct Answer");
        correctCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCorrectAnswerText()
        ));

        table.getColumns().addAll(qCol, diffCol, correctCol);

        VBox centerBox = new VBox(10, table);
        centerBox.setPadding(new Insets(10, 30, 10, 30));
        setCenter(centerBox);

        // ---------------------------------------------------------------------
        // Bottom actions
        // ---------------------------------------------------------------------
        HBox bottom = new HBox(10);
        bottom.setPadding(new Insets(10, 30, 20, 30));
        bottom.setAlignment(Pos.CENTER_RIGHT);

        styleSecondary(editBtn);
        styleSecondary(deleteBtn);

        bottom.getChildren().addAll(editBtn, deleteBtn);
        setBottom(bottom);
    }

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

    private void styleSecondary(Button btn) {
        btn.setPrefHeight(32);
        btn.setFont(Font.font("Arial", 13));
        btn.setStyle(
            "-fx-background-color: #1F2937;" +
            "-fx-text-fill: #E5E7EB;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 0 14 0 14;" +
            "-fx-cursor: hand;"
        );
    }
}
