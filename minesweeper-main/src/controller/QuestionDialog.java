package controller;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import model.Question;
import model.QuestionDifficulty;
import model.SysData;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Dialog shown when a Question Cell is activated.
 * Picks a random question from QuestionRepository and returns:
 *  - result: true  -> player answered correctly
 *  - result: false -> player answered incorrectly
 *
 * GameController also calls getQuestionDifficulty() to know
 * if it was Easy / Intermediate / Hard / Expert for scoring.
 */
public class QuestionDialog {

    private final Dialog<Boolean> dialog;
    private String questionDifficulty;   // "Easy", "Intermediate", "Hard", "Expert"
    private int correctIndex;

    public QuestionDialog(String gameDifficulty) {

        dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Load all questions from CSV
        List<Question> allQuestions = SysData.loadQuestions();
        Question chosen = null;

        if (allQuestions != null && !allQuestions.isEmpty()) {
            chosen = allQuestions.get(new Random().nextInt(allQuestions.size()));

            QuestionDifficulty diffEnum = chosen.getDifficulty();
            questionDifficulty = mapDifficultyLabel(diffEnum);

            dialog.setTitle("Question Cell");
            dialog.setHeaderText(questionDifficulty + " Question");
        } else {
            // fallback if CSV is empty / missing
            String[] diffs = {"Easy", "Intermediate", "Hard", "Expert"};
            questionDifficulty = diffs[new Random().nextInt(diffs.length)];
            dialog.setTitle("Question Cell");
            dialog.setHeaderText(questionDifficulty + " Question");
        }

        VBox box = new VBox(12);
        box.setPadding(new Insets(20));

        String qText;
        String[] answers;

        if (chosen != null) {
            qText = chosen.getText();
            answers = chosen.getOptions();
            correctIndex = chosen.getCorrectIndex();

            // safety: ensure 4 options
            if (answers == null || answers.length < 4) {
                answers = new String[]{"A", "B", "C", "D"};
                correctIndex = 0;
            }
        } else {
            qText = "No questions found in CSV.";
            answers = new String[]{"A", "B", "C", "D"};
            correctIndex = 0;
        }

        Label question = new Label(qText);
        question.setWrapText(true);
        question.setMaxWidth(400);
        box.getChildren().add(question);
        box.getChildren().add(new Label("")); // spacer

        // 4 answer buttons A–D
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            Button btn = new Button((char) ('A' + i) + ") " + answers[i]);
            btn.setPrefWidth(400);
            btn.setPrefHeight(45);

            btn.setOnAction(e -> {
                dialog.setResult(idx == correctIndex); // true if correct
                dialog.close();
            });

            box.getChildren().add(btn);
        }

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().clear(); // no default OK/Cancel
        dialog.getDialogPane().setPrefSize(480, 300);
    }

    /**
     * Map enum difficulty from Question to string used by GameController.
     */
    private String mapDifficultyLabel(QuestionDifficulty diff) {
        if (diff == null) return "Easy";
        switch (diff) {
            case EASY:    return "Easy";
            case MEDIUM:  return "Intermediate";
            case HARD:    return "Hard";
            case EXPERT:  return "Expert";
            default:      return "Easy";
        }
    }

    public String getQuestionDifficulty() {
        return questionDifficulty;
    }

    public Optional<Boolean> showAndWait() {
        return dialog.showAndWait();
    }
}
