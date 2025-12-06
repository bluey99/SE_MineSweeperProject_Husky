package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.stage.Stage;
import model.Question;
import model.SysData;
import view.QuestionFormDialog;
import view.QuestionManagementView;

import java.util.List;
import java.util.Optional;

public class QuestionManagementController {

    private final Stage primaryStage;
    public final QuestionManagementView view;
    private final ObservableList<Question> questionList;

    public QuestionManagementController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.view = new QuestionManagementView();

        List<Question> loaded = SysData.loadQuestions();
        this.questionList = FXCollections.observableArrayList(loaded);
        view.table.setItems(questionList);

        setupHandlers();
    }

    private void setupHandlers() {

        // ----------------------- BACK BUTTON -----------------------
        view.backBtn.setOnAction(e -> {
            Main.showMainMenu(primaryStage);
        });

        // ----------------------- ADD QUESTION -----------------------
        view.addBtn.setOnAction(e -> {
            QuestionFormDialog dialog = new QuestionFormDialog("Add New Question", null);
            Optional<Question> result = dialog.showAndWait();

            result.ifPresent(q -> {
<<<<<<< Updated upstream

                //  prevent empty question crash
                if (q.getText() == null || q.getText().trim().isEmpty()) {
                    showError("Invalid Input", "Question cannot be empty.");
                    return;
                }

                try {
                    // Try to save to CSV
                    QuestionRepository.addQuestion(q);
                    refreshFromRepository();
                } catch (UnsupportedOperationException ex) {
                    // CSV mode does not support adding
                    showError(
                            "Operation Not Supported",
                            "Adding questions is disabled in CSV storage mode."
                    );
                }
=======
                SysData.addQuestion(q);
                refreshQuestions();
>>>>>>> Stashed changes
            });
        });

        // ----------------------- EDIT QUESTION -----------------------
        view.editBtn.setOnAction(e -> {
            Question selected = view.table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            QuestionFormDialog dialog = new QuestionFormDialog("Edit Question", selected);
            Optional<Question> result = dialog.showAndWait();

            result.ifPresent(q -> {
<<<<<<< Updated upstream

                // - validation for empty editing
                if (q.getText() == null || q.getText().trim().isEmpty()) {
                    showError("Invalid Input", "Question cannot be empty.");
                    return;
                }

                try {
                    QuestionRepository.updateQuestion(q);
                    refreshFromRepository();
                } catch (UnsupportedOperationException ex) {
                    showError("Operation Not Supported",
                              "Editing questions is disabled in CSV storage mode.");
                }
=======
                SysData.updateQuestion(q);
                refreshQuestions();
>>>>>>> Stashed changes
            });
        });

        // ----------------------- DELETE QUESTION -----------------------
        view.deleteBtn.setOnAction(e -> {
            Question selected = view.table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("No Selection", "Please select a question to delete.");
                return;
            }

<<<<<<< Updated upstream
            // bayan added here – confirmation dialog before deleting
            Alert confirm = new Alert(Alert.AlertType.WARNING);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Are you sure you want to delete this question?");
            confirm.setContentText("Question: " + selected.getText());

            ButtonType yesBtn = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            confirm.getButtonTypes().setAll(yesBtn, cancelBtn);

            confirm.showAndWait().ifPresent(response -> {
                if (response == yesBtn) {
                    try {
                        QuestionRepository.deleteQuestion(selected);
                        refreshFromRepository();
                    } catch (UnsupportedOperationException ex) {
                        showError("Operation Not Supported",
                                  "Deleting questions is disabled in CSV mode.");
                    }
                }
            });
        });
    }

    // ----------------------- ERROR POPUP -----------------------
    //  reusable alert popup
    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ----------------------- REFRESH TABLE -----------------------
    private void refreshFromRepository() {
        questionList.setAll(QuestionRepository.loadQuestions());
=======
            SysData.deleteQuestion(selected);
            refreshQuestions();
        });
    }

    private void refreshQuestions() {
        questionList.setAll(SysData.loadQuestions());
>>>>>>> Stashed changes
    }

    // ----------------------- SHOW SCENE -----------------------
    public Scene createScene() {
        return new Scene(view, 900, 600);
    }
}
