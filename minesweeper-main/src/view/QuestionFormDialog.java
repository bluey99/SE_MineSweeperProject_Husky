package view;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.Question;
import model.QuestionDifficulty;

import java.util.Optional;

public class QuestionFormDialog {

    private final Dialog<Question> dialog = new Dialog<>();

    private final TextArea questionArea = new TextArea();

    private final TextField[] optionFields = {
            new TextField(), new TextField(), new TextField(), new TextField()
    };

    private final ComboBox<String> correctAnswerBox = new ComboBox<>();
    private final ToggleGroup difficultyGroup = new ToggleGroup();

    private final boolean isEditMode;
    private final Question original;

    public QuestionFormDialog(String title, Question existing) {
        this.isEditMode = (existing != null);
        this.original = existing;

        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setResizable(true);

        ButtonType okType = new ButtonType(
                isEditMode ? "Save Changes" : "Add Question",
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, cancelType);

        // Size relative to Question Management window
        dialog.setOnShown(e -> {
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            Window owner = dialogStage.getOwner();

            if (owner != null) {
                dialog.getDialogPane().setPrefWidth(owner.getWidth() * 0.72);
                dialog.getDialogPane().setPrefHeight(owner.getHeight() * 0.72);
            } else {
                dialog.getDialogPane().setPrefWidth(640);
                dialog.getDialogPane().setPrefHeight(560);
            }
        });

        VBox root = new VBox(14);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #0f172a;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: white;");

        Label subtitle = new Label(isEditMode
                ? "Update the question details"
                : "Create a new trivia question");
        subtitle.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");

        // ---------------- Question (NO sectionBox) ----------------
        Label qLabel = sectionLabel("Question");

        questionArea.setPromptText("Enter your question...");
        questionArea.setWrapText(true);
        questionArea.setPrefRowCount(2);
        questionArea.setPrefHeight(80);

        // ✅ Make TextArea itself transparent (removes the “inner/bottom box” effect)
        // Keep the black text like you wanted
        questionArea.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-control-inner-background: transparent;" +
                "-fx-text-fill: #000000;" +
                "-fx-prompt-text-fill: #6B7280;" +
                "-fx-caret-color: #000000;" +
                "-fx-background-insets: 0;" +
                "-fx-border-color: transparent;" +
                "-fx-faint-focus-color: transparent;" +
                "-fx-focus-color: transparent;"
        );

        // ✅ One single visible white box (this replaces any “extra box” look)
        StackPane questionInputBox = new StackPane(questionArea);
        questionInputBox.setPadding(new Insets(10, 12, 10, 12));
        questionInputBox.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-border-color: rgba(0,0,0,0.10);"
        );

        VBox questionBox = new VBox(8, qLabel, questionInputBox);
        questionBox.setPadding(new Insets(0));

        // ---------------- Answers ----------------
        Label aLabel = sectionLabel("Answer Options");

        GridPane answersGrid = new GridPane();
        answersGrid.setHgap(12);
        answersGrid.setVgap(10);

        answersGrid.getColumnConstraints().addAll(
                new ColumnConstraints(50, 50, Double.MAX_VALUE, Priority.ALWAYS, null, true),
                new ColumnConstraints(50, 50, Double.MAX_VALUE, Priority.ALWAYS, null, true)
        );

        addAnswerField(answersGrid, 0, 0, "A", optionFields[0]);
        addAnswerField(answersGrid, 1, 0, "B", optionFields[1]);
        addAnswerField(answersGrid, 0, 1, "C", optionFields[2]);
        addAnswerField(answersGrid, 1, 1, "D", optionFields[3]);

        // ---------------- Correct answer ----------------
        Label correctLabel = sectionLabel("Correct Answer");

        correctAnswerBox.getItems().addAll("A", "B", "C", "D");
        correctAnswerBox.getSelectionModel().selectFirst();
        correctAnswerBox.setPrefHeight(36);
        correctAnswerBox.setMaxWidth(Double.MAX_VALUE);
        correctAnswerBox.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-text-fill: #000000;" +
                "-fx-prompt-text-fill: #6B7280;" +
                "-fx-background-radius: 10;"
        );

        // ---------------- Difficulty ----------------
        Label dLabel = sectionLabel("Difficulty");

        ToggleButton easyBtn = makeDiffButton("Easy");
        ToggleButton medBtn = makeDiffButton("Medium");
        ToggleButton hardBtn = makeDiffButton("Hard");
        ToggleButton expertBtn = makeDiffButton("Expert");

        HBox diffBox = new HBox(8, easyBtn, medBtn, hardBtn, expertBtn);
        diffBox.setAlignment(Pos.CENTER_LEFT);
        easyBtn.setSelected(true);

        // Load edit data
        if (isEditMode && original != null) {
            questionArea.setText(original.getText());

            String[] opts = original.getOptions();
            for (int i = 0; i < 4; i++) {
                if (opts != null && i < opts.length) {
                    optionFields[i].setText(opts[i]);
                }
            }

            correctAnswerBox.getSelectionModel().select(original.getCorrectIndex());

            QuestionDifficulty diff = original.getDifficulty();
            switch (diff) {
                case MEDIUM -> medBtn.setSelected(true);
                case HARD -> hardBtn.setSelected(true);
                case EXPERT -> expertBtn.setSelected(true);
                default -> easyBtn.setSelected(true);
            }
        }

        root.getChildren().addAll(
                titleLabel,
                subtitle,
                questionBox, // ✅ fixed
                sectionBox(aLabel, answersGrid),
                sectionBox(correctLabel, correctAnswerBox),
                sectionBox(dLabel, diffBox)
        );

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().setStyle("-fx-background-color: #0f172a;");

        // ---------------- Validation ----------------
        Button okButton = (Button) dialog.getDialogPane().lookupButton(okType);

        okButton.addEventFilter(ActionEvent.ACTION, evt -> {
            String qText = questionArea.getText().trim();

            if (qText.isEmpty()) {
                showError("Invalid Question", "Please enter a question.");
                evt.consume();
                return;
            }

            if (!qText.matches(".*[A-Za-z].*")) {
                showError("Invalid Question", "Please Enter a valid question.");
                evt.consume();
                return;
            }

            for (TextField tf : optionFields) {
                if (tf.getText().trim().isEmpty()) {
                    showError("Invalid Answers", "All 4 answers must be filled.");
                    evt.consume();
                    return;
                }
            }

            if (difficultyGroup.getSelectedToggle() == null) {
                showError("Missing Difficulty", "Please choose a difficulty level.");
                evt.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType.getButtonData() != ButtonBar.ButtonData.OK_DONE) return null;

            String[] opts = new String[4];
            for (int i = 0; i < 4; i++) {
                opts[i] = optionFields[i].getText().trim();
            }

            int correctIdx = correctAnswerBox.getSelectionModel().getSelectedIndex();
            QuestionDifficulty diff = QuestionDifficulty.fromString(
                    ((ToggleButton) difficultyGroup.getSelectedToggle()).getText()
            );

            if (isEditMode && original != null) {
                return new Question(original.getId(), questionArea.getText().trim(), opts, correctIdx, diff);
            } else {
                return new Question(questionArea.getText().trim(), opts, correctIdx, diff);
            }
        });
    }

    // ---------------- UI helpers ----------------
    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #E5E7EB; -fx-font-size: 13px; -fx-font-weight: bold;");
        return l;
    }

    private VBox sectionBox(javafx.scene.Node... nodes) {
        VBox box = new VBox(8, nodes);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04);" +
                "-fx-background-radius: 14;"
        );
        return box;
    }

    private void addAnswerField(GridPane grid, int col, int row, String letter, TextField tf) {
        Label chip = new Label(letter);
        chip.setMinWidth(26);
        chip.setAlignment(Pos.CENTER);
        chip.setStyle(
                "-fx-background-color: rgba(37,99,235,0.20);" +
                "-fx-text-fill: #1E40AF;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;"
        );

        tf.setPrefHeight(36);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-text-fill: #000000;" +
                "-fx-prompt-text-fill: #6B7280;" +
                "-fx-background-radius: 10;" +
                "-fx-caret-color: #000000;"
        );

        HBox wrap = new HBox(8, chip, tf);
        wrap.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tf, Priority.ALWAYS);

        grid.add(wrap, col, row);
    }

    private ToggleButton makeDiffButton(String text) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(difficultyGroup);
        btn.setPrefHeight(32);
        btn.setMinWidth(100);
        btn.setStyle(
                "-fx-background-color: #111827;" +
                "-fx-text-fill: #E5E7EB;" +
                "-fx-background-radius: 10;"
        );

        btn.selectedProperty().addListener((obs, o, sel) -> {
            if (sel) {
                btn.setStyle(
                        "-fx-background-color: #2563EB;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;"
                );
            } else {
                btn.setStyle(
                        "-fx-background-color: #111827;" +
                        "-fx-text-fill: #E5E7EB;" +
                        "-fx-background-radius: 10;"
                );
            }
        });
        return btn;
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public Optional<Question> showAndWait() {
        return dialog.showAndWait();
    }
}
