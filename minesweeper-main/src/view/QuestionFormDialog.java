package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Question;
import model.QuestionDifficulty;

import java.util.Optional;

public class QuestionFormDialog {

    private final Dialog<Question> dialog = new Dialog<>();

    private final TextField questionField = new TextField();
    private final TextField[] optionFields = {
            new TextField(), new TextField(), new TextField(), new TextField()
    };
    private final ToggleGroup correctGroup = new ToggleGroup();
    private final ToggleGroup difficultyGroup = new ToggleGroup();

    private final boolean isEditMode;
    private final Question original;

    public QuestionFormDialog(String title, Question existing) {
        this.isEditMode = (existing != null);
        this.original = existing;

        dialog.setTitle(title);
        dialog.setHeaderText(null);

        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType(isEditMode ? "Save Changes" : "Add Question", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        VBox root = new VBox(18);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0f172a;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: white;");

        // Question
        Label qLabel = new Label("Question");
        qLabel.setStyle("-fx-text-fill: #E5E7EB;");
        questionField.setPromptText("Enter your question...");
        questionField.setStyle("-fx-background-color: #111827; -fx-text-fill: #E5E7EB; -fx-background-radius: 6;");
        questionField.setPrefHeight(40);

        // Answers
        Label aLabel = new Label("Answer Options");
        aLabel.setStyle("-fx-text-fill: #E5E7EB;");

        VBox answersBox = new VBox(8);
        for (int i = 0; i < 4; i++) {
            HBox row = new HBox(8);
            RadioButton rb = new RadioButton();
            rb.setToggleGroup(correctGroup);
            rb.setUserData(i);

            TextField tf = optionFields[i];
            tf.setPromptText("Answer " + (i + 1));
            tf.setStyle("-fx-background-color: #111827; -fx-text-fill: #E5E7EB; -fx-background-radius: 6;");
            tf.setPrefHeight(36);

            row.getChildren().addAll(rb, tf);
            answersBox.getChildren().add(row);
        }
        ((RadioButton) correctGroup.getToggles().get(0)).setSelected(true);

        Label correctHint = new Label("Select the radio button for the correct answer");
        correctHint.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

        // Difficulty
        Label dLabel = new Label("Difficulty");
        dLabel.setStyle("-fx-text-fill: #E5E7EB;");

        ToggleButton easyBtn = makeDiffButton("Easy");
        ToggleButton medBtn = makeDiffButton("Medium");
        ToggleButton hardBtn = makeDiffButton("Hard");
        ToggleButton expertBtn = makeDiffButton("Expert");

        HBox diffBox = new HBox(10, easyBtn, medBtn, hardBtn, expertBtn);
        diffBox.setAlignment(Pos.CENTER_LEFT);

        // Default difficulty = Easy
        easyBtn.setSelected(true);

        // If editing, fill with existing data
        if (isEditMode && original != null) {
            questionField.setText(original.getText());
            String[] opts = original.getOptions();
            for (int i = 0; i < 4; i++) {
                if (opts != null && i < opts.length) {
                    optionFields[i].setText(opts[i]);
                }
            }
            int idx = original.getCorrectIndex();
            if (idx >= 0 && idx < 4) {
                correctGroup.selectToggle(correctGroup.getToggles().get(idx));
            }
            QuestionDifficulty diff = original.getDifficulty();
            switch (diff) {
                case MEDIUM: medBtn.setSelected(true); break;
                case HARD: hardBtn.setSelected(true); break;
                case EXPERT: expertBtn.setSelected(true); break;
                default: easyBtn.setSelected(true);
            }
        }

        root.getChildren().addAll(
                titleLabel,
                qLabel, questionField,
                aLabel, answersBox, correctHint,
                dLabel, diffBox
        );

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().setStyle("-fx-background-color: #0f172a;");

        dialog.setResultConverter(buttonType -> {
            if (buttonType.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return null;
            }

            String qText = questionField.getText().trim();
            String[] opts = new String[4];
            for (int i = 0; i < 4; i++) {
                opts[i] = optionFields[i].getText().trim();
            }

            Toggle selected = correctGroup.getSelectedToggle();
            int correctIdx = selected == null ? 0 : (int) selected.getUserData();

            Toggle diffToggle = difficultyGroup.getSelectedToggle();
            String diffStr = diffToggle == null ? "Easy" : ((ToggleButton) diffToggle).getText();
            QuestionDifficulty diff = QuestionDifficulty.fromString(diffStr);

            Question q;
            if (isEditMode && original != null) {
                q = new Question(original.getId(), qText, opts, correctIdx, diff);
            } else {
                q = new Question(qText, opts, correctIdx, diff);
            }
            return q;
        });
    }

    private ToggleButton makeDiffButton(String text) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(difficultyGroup);
        btn.setPrefHeight(32);
        btn.setStyle(
            "-fx-background-color: #111827;" +
            "-fx-text-fill: #E5E7EB;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );

        btn.selectedProperty().addListener((obs, wasSel, isSel) -> {
            if (isSel) {
                btn.setStyle(
                    "-fx-background-color: #2563EB;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;"
                );
            } else {
                btn.setStyle(
                    "-fx-background-color: #111827;" +
                    "-fx-text-fill: #E5E7EB;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;"
                );
            }
        });
        return btn;
    }

    public Optional<Question> showAndWait() {
        return dialog.showAndWait();
    }
}
