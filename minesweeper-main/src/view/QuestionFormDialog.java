package view;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.Question;
import model.QuestionDifficulty;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

    // ---------------- Theme ----------------
    private static final String BG = "#0f172a";
    private static final String CARD_BG = "rgba(255,255,255,0.04)";
    private static final String BORDER = "#1E293B";
    private static final String INPUT_BG = "#020617";
    private static final String TEXT = "#E5E7EB";
    private static final String SUBTEXT = "#9CA3AF";
    private static final String PLACEHOLDER = "#6B7280";
    private static final String PRIMARY = "#2563EB";
    private static final String SECONDARY = "#1F2937";
    private static final String SECONDARY_HOVER = "#334155";

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

        // Cancel first, OK last => OK button appears on the right
        dialog.getDialogPane().getButtonTypes().addAll(cancelType, okType);

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

            // ✅ Remove the “dots” (TextArea scrollbar corner artifacts)
            questionArea.lookupAll(".scroll-bar").forEach(n -> n.setVisible(false));
            questionArea.lookupAll(".corner").forEach(n -> n.setVisible(false));
        });

        VBox root = new VBox(14);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: " + BG + ";");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 26));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitle = new Label(isEditMode
                ? "Update the question details"
                : "Create a new trivia question");
        subtitle.setStyle("-fx-text-fill: " + SUBTEXT + "; -fx-font-size: 13px;");

        // ---------------- Question ----------------
        Label qLabel = sectionLabel("Question");

        questionArea.setPromptText("Enter your question...");
        questionArea.setWrapText(true);
        questionArea.setPrefRowCount(3);

        // fixed height so it looks stable (no resizing artifacts)
        questionArea.setMinHeight(100);
        questionArea.setPrefHeight(100);
        questionArea.setMaxHeight(100);

        // hide scrollbars (still created internally, but we hide them onShown too)
        questionArea.setStyle(""); // clear first
        styleTextArea(questionArea);

        VBox questionBox = sectionBox(qLabel, questionArea);

        // ---------------- Answers ----------------
        Label aLabel = sectionLabel("Answer Options");

        GridPane answersGrid = new GridPane();
        answersGrid.setHgap(12);
        answersGrid.setVgap(10);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        answersGrid.getColumnConstraints().addAll(c1, c2);

        addAnswerField(answersGrid, 0, 0, "A", optionFields[0]);
        addAnswerField(answersGrid, 1, 0, "B", optionFields[1]);
        addAnswerField(answersGrid, 0, 1, "C", optionFields[2]);
        addAnswerField(answersGrid, 1, 1, "D", optionFields[3]);

        VBox answersBox = sectionBox(aLabel, answersGrid);

        // ---------------- Correct answer ----------------
        Label correctLabel = sectionLabel("Correct Answer");

        correctAnswerBox.getItems().addAll("A", "B", "C", "D");
        correctAnswerBox.getSelectionModel().selectFirst();
        correctAnswerBox.setPrefHeight(36);
        correctAnswerBox.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(correctAnswerBox);

        VBox correctBox = sectionBox(correctLabel, correctAnswerBox);

        // ---------------- Difficulty ----------------
        Label dLabel = sectionLabel("Difficulty");

        ToggleButton easyBtn = makeDiffButton("Easy");
        ToggleButton medBtn = makeDiffButton("Medium");
        ToggleButton hardBtn = makeDiffButton("Hard");
        ToggleButton expertBtn = makeDiffButton("Expert");

        HBox diffBox = new HBox(10, easyBtn, medBtn, hardBtn, expertBtn);
        diffBox.setAlignment(Pos.CENTER_LEFT);
        easyBtn.setSelected(true);

        VBox difficultyBox = sectionBox(dLabel, diffBox);

        // Load edit data
        if (isEditMode && original != null) {
            questionArea.setText(original.getText());

            String[] opts = original.getOptions();
            for (int i = 0; i < 4; i++) {
                if (opts != null && i < opts.length) {
                    optionFields[i].setText(opts[i]);
                }
            }

            // original.getCorrectIndex() should be 0..3
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
                questionBox,
                answersBox,
                correctBox,
                difficultyBox
        );

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().setStyle("-fx-background-color: " + BG + ";");

        // ---------------- Buttons (match the rest of the system) ----------------
        Button okButton = (Button) dialog.getDialogPane().lookupButton(okType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelType);

        stylePrimary(okButton);
        styleSecondary(cancelButton);

        // ---------------- Validation ----------------
        okButton.addEventFilter(ActionEvent.ACTION, evt -> {
            String qText = questionArea.getText().trim();

            if (qText.isEmpty()) {
                showError("Invalid Question", "Please enter a question.");
                evt.consume();
                return;
            }

            if (!qText.matches(".*[A-Za-z].*")) {
                showError("Invalid Question", "Please enter a valid question.");
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

            // Prevent duplicate answers (case-insensitive, after trimming)
            Set<String> uniqueAnswers = new HashSet<>();
            for (TextField tf : optionFields) {
                uniqueAnswers.add(tf.getText().trim().toLowerCase());
            }

            if (uniqueAnswers.size() < optionFields.length) {
                showError("Invalid Answers", "All answer options must be different.");
                evt.consume();
                return;
            }

            if (difficultyGroup.getSelectedToggle() == null) {
                showError("Missing Difficulty", "Please choose a difficulty level.");
                evt.consume();
                return;
            }

            // ✅ CSV safety: block characters that break CSV parsing in your system
            // (commas/newlines shift columns and can make Correct Answer empty)
            if (containsCsvBreakingChars(qText)) {
                showError("Invalid Characters",
                        "Please remove commas (,) and new lines from the question.\n" +
                        "Your system saves questions to CSV, and commas/new lines break the file format.");
                evt.consume();
                return;
            }

            for (TextField tf : optionFields) {
                if (containsCsvBreakingChars(tf.getText())) {
                    showError("Invalid Characters",
                            "Please remove commas (,) and new lines from the answers.\n" +
                            "CSV format will break otherwise.");
                    evt.consume();
                    return;
                }
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType.getButtonData() != ButtonBar.ButtonData.OK_DONE) return null;

            // ✅ sanitize anyway (extra protection)
            String q = sanitizeForCsv(questionArea.getText());

            String[] opts = new String[4];
            for (int i = 0; i < 4; i++) {
                opts[i] = sanitizeForCsv(optionFields[i].getText());
            }

            int correctIdx = correctAnswerBox.getSelectionModel().getSelectedIndex(); // 0..3
            QuestionDifficulty diff = QuestionDifficulty.fromString(
                    ((ToggleButton) difficultyGroup.getSelectedToggle()).getText()
            );

            if (isEditMode && original != null) {
                return new Question(original.getId(), q, opts, correctIdx, diff);
            } else {
                return new Question(q, opts, correctIdx, diff);
            }
        });
    }

    // ---------------- CSV safety helpers ----------------
    private boolean containsCsvBreakingChars(String s) {
        if (s == null) return false;
        return s.contains(",") || s.contains("\n") || s.contains("\r");
    }

    // Extra protection: normalize spaces + remove line breaks + replace commas
    private String sanitizeForCsv(String s) {
        if (s == null) return "";
        s = s.replace("\r", " ").replace("\n", " ").trim();
        s = s.replaceAll("\\s+", " ");
        s = s.replace(",", ";");
        return s;
    }

    // ---------------- UI helpers ----------------
    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        return l;
    }

    private VBox sectionBox(Node... nodes) {
        VBox box = new VBox(8, nodes);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: rgba(30,41,59,0.65);" +
                        "-fx-border-radius: 14;"
        );
        return box;
    }

    private void styleTextField(TextField tf) {
        tf.setPrefHeight(36);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle(
                "-fx-background-color: " + INPUT_BG + ";" +
                        "-fx-text-fill: " + TEXT + ";" +
                        "-fx-prompt-text-fill: " + PLACEHOLDER + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-caret-color: " + TEXT + ";" +
                        "-fx-padding: 0 10 0 10;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;"
        );
    }

    private void styleTextArea(TextArea ta) {
        ta.setStyle(
                "-fx-background-color: " + INPUT_BG + ";" +
                        "-fx-control-inner-background: " + INPUT_BG + ";" +
                        "-fx-text-fill: " + TEXT + ";" +
                        "-fx-prompt-text-fill: " + PLACEHOLDER + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-caret-color: " + TEXT + ";" +
                        "-fx-padding: 10;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-highlight-fill: rgba(37,99,235,0.35);" +

                        // ✅ hide internal corner + scrollbar backgrounds
                        "-fx-background-insets: 0;" +
                        "-fx-control-inner-background-insets: 0;"
        );
    }

    private void styleComboBox(ComboBox<String> cb) {
        cb.setStyle(
                "-fx-background-color: " + INPUT_BG + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 2 6 2 6;"
        );

        cb.setButtonCell(makeDarkListCell());
        cb.setCellFactory(list -> makeDarkListCell());
    }

    private ListCell<String> makeDarkListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(Color.web(TEXT));
                    setStyle("-fx-background-color: " + INPUT_BG + ";");
                }
            }
        };
    }

    private void addAnswerField(GridPane grid, int col, int row, String letter, TextField tf) {
        Label chip = new Label(letter);
        chip.setMinWidth(28);
        chip.setMinHeight(28);
        chip.setAlignment(Pos.CENTER);
        chip.setStyle(
                "-fx-background-color: rgba(37,99,235,0.20);" +
                        "-fx-text-fill: #93C5FD;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 999;"
        );

        styleTextField(tf);

        HBox wrap = new HBox(10, chip, tf);
        wrap.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tf, Priority.ALWAYS);

        grid.add(wrap, col, row);
    }

    private ToggleButton makeDiffButton(String text) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(difficultyGroup);
        btn.setPrefHeight(34);
        btn.setMinWidth(110);

        String off = "-fx-background-color: #111827;" +
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: rgba(30,41,59,0.7);" +
                "-fx-border-radius: 10;";

        String on = "-fx-background-color: " + PRIMARY + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: transparent;";

        btn.setStyle(off);
        btn.selectedProperty().addListener((obs, o, sel) -> btn.setStyle(sel ? on : off));
        return btn;
    }

    private void stylePrimary(Button btn) {
        btn.setPrefHeight(34);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setStyle(
                "-fx-background-color: " + PRIMARY + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 0 16 0 16;" +
                        "-fx-cursor: hand;"
        );
    }

    private void styleSecondary(Button btn) {
        btn.setPrefHeight(34);
        btn.setFont(Font.font("Arial", 13));

        String normal = "-fx-background-color: " + SECONDARY + ";" +
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 0 16 0 16;" +
                "-fx-cursor: hand;";

        String hover = "-fx-background-color: " + SECONDARY_HOVER + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 0 16 0 16;" +
                "-fx-cursor: hand;";

        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
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
