package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import model.ImportReport;
import model.Question;
import model.QuestionsFileStatus;
import model.SysData;

import view.QuestionFormDialog;
import view.QuestionManagementView;

import view.dialogs.ConfirmDialog;
import view.dialogs.InfoDialog;
import view.dialogs.ErrorDialog;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Question Management screen.
 * Manages user interactions and coordinates between the view and the model.
 *
 * Merged version:
 * - Uses the new Template Method dialog system (InfoDialog / ErrorDialog / ConfirmDialog)
 * - Keeps the Import JSON button behavior (FileChooser + SysData.importQuestionsFromJson)
 * - Preserves safety rules (disable actions when CSV is malformed, including Import)
 */
public class QuestionManagementController {

    private final Stage primaryStage;

    // View managed by this controller
    public final QuestionManagementView view;

    // Backing list for the table view
    private final ObservableList<Question> questionList;

    /**
     * Initializes the Question Management screen and validates file state.
     */
    public QuestionManagementController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.view = new QuestionManagementView();

        // Determine questions file state
        QuestionsFileStatus status = SysData.getQuestionsFileStatus();

        // Load questions only if file is not malformed
        List<Question> loaded =
                (status == QuestionsFileStatus.MALFORMED)
                        ? List.of()
                        : SysData.loadQuestions();

        this.questionList = FXCollections.observableArrayList(loaded);
        view.table.setItems(questionList);

        // Disable edit/delete until selection
        view.editBtn.setDisable(true);
        view.deleteBtn.setDisable(true);

        switch (status) {

            case NOT_EXISTS ->
                    new InfoDialog(
                            "Questions File Missing",
                            "No questions file was found.\n\n"
                                    + "The file will be created automatically when you add the first question."
                    ).show();

            case MALFORMED -> {
                new ErrorDialog(
                        "Invalid Questions File",
                        "The questions file exists but is not formatted correctly.\n\n"
                                + "Please fix or replace the file before managing questions."
                ).show();

                // Lock all modification actions
                view.addBtn.setDisable(true);
                view.editBtn.setDisable(true);
                view.deleteBtn.setDisable(true);

                // ✅ also lock import when file is malformed
                view.importJsonBtn.setDisable(true);
            }

            case EMPTY, HAS_DATA -> {
                // No popup needed
            }
        }

        setupHandlers();
    }

    /**
     * Registers all UI event handlers.
     */
    private void setupHandlers() {

        // Navigate back to the main menu
        view.backBtn.setOnAction(e -> {
            GameController.resetInstance();
            Main.showMainMenu(primaryStage);
        });

        // Enable Edit/Delete only when a table row is selected
        view.table.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> {

                    boolean hasSelection = newSel != null;
                    view.editBtn.setDisable(!hasSelection);
                    view.deleteBtn.setDisable(!hasSelection);
                });

        // -------------------- IMPORT JSON --------------------
        view.importJsonBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Import Questions from JSON");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );

            File jsonFile = chooser.showOpenDialog(primaryStage);
            if (jsonFile == null) return;

            ImportReport report = SysData.importQuestionsFromJson(jsonFile);

            if (report == null || !report.success) {
                String msg = (report == null)
                        ? "Import failed (unknown error)."
                        : report.message;

                new ErrorDialog("Import Failed", msg).show();
                return;
            }

            new InfoDialog("Import Completed", report.message).show();
            refreshQuestions();
        });

        // -------------------- ADD QUESTION --------------------
        view.addBtn.setOnAction(e -> {
            QuestionFormDialog dialog =
                    new QuestionFormDialog("Add New Question", null);

            Optional<Question> result = dialog.showAndWait();

            result.ifPresent(q -> {
                boolean success = SysData.addQuestion(q);

                if (success) {
                    new InfoDialog(
                            "Question Added",
                            "The question was added successfully."
                    ).show();

                    refreshQuestions();
                } else {
                    new ErrorDialog(
                            "Add Failed",
                            "Unable to save the question.\n\n"
                                    + "The questions file may be open in another program.\n"
                                    + "Please close it and try again."
                    ).show();
                }
            });
        });

        // -------------------- EDIT QUESTION --------------------
        view.editBtn.setOnAction(e -> {
            Question selected =
                    view.table.getSelectionModel().getSelectedItem();

            if (selected == null) return;

            QuestionFormDialog dialog =
                    new QuestionFormDialog("Edit Question", selected);

            Optional<Question> result = dialog.showAndWait();

            result.ifPresent(q -> {
                boolean success = SysData.updateQuestion(q);

                if (success) {
                    new InfoDialog(
                            "Question Updated",
                            "The question was updated successfully."
                    ).show();

                    refreshQuestions();
                } else {
                    new ErrorDialog(
                            "Update Failed",
                            "Unable to update the question.\n\n"
                                    + "The questions file may be open in another program.\n"
                                    + "Please close it and try again."
                    ).show();
                }
            });
        });

        // -------------------- DELETE QUESTION --------------------
        view.deleteBtn.setOnAction(e -> {
            Question selected =
                    view.table.getSelectionModel().getSelectedItem();

            if (selected == null) return;

            // confirm delete
            if (new ConfirmDialog(
                    "Delete Question",
                    "Are you sure you want to delete the selected question?"
            ).show() != ButtonType.OK) return;

            boolean success = SysData.deleteQuestion(selected);

            if (success) {
                new InfoDialog(
                        "Question Deleted",
                        "The question was deleted successfully."
                ).show();

                refreshQuestions();
            } else {
                new ErrorDialog(
                        "Delete Failed",
                        "Unable to delete the question.\n\n"
                                + "The questions file may be open in another program.\n"
                                + "Please close it and try again."
                ).show();
            }
        });
    }

    /**
     * Reloads questions after add/edit/delete operations.
     */
    private void refreshQuestions() {
        questionList.setAll(SysData.loadQuestions());
    }
}
