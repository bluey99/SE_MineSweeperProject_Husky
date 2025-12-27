package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Question;
import model.QuestionsFileStatus;
import model.SysData;
import view.DeleteQuestionConfirmDialog;
import view.QuestionFormDialog;
import view.QuestionManagementView;

import java.util.List;
import java.util.Optional;

/**
 * Controller for the Question Management screen. Manages user interactions and
 * coordinates between the view and the model.
 */

public class QuestionManagementController {

	private final Stage primaryStage;

	// View managed by this controller
	public final QuestionManagementView view;

	// Backing list for the table view
	private final ObservableList<Question> questionList;

	// Initializes the Question Management screen and validates file state
	public QuestionManagementController(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.view = new QuestionManagementView();

		// Determine questions file state
		QuestionsFileStatus status = SysData.getQuestionsFileStatus();

		// Load questions only if file is not malformed
		List<Question> loaded = (status == QuestionsFileStatus.MALFORMED) ? List.of() : SysData.loadQuestions();

		this.questionList = FXCollections.observableArrayList(loaded);
		view.table.setItems(questionList);

		// Disable edit/delete until selection
		view.editBtn.setDisable(true);
		view.deleteBtn.setDisable(true);

		switch (status) {

		case NOT_EXISTS -> showInfo("Questions File Missing", "No questions file was found.\n\n"
				+ "The file will be created automatically when you add the first question.");

		case MALFORMED -> {
			showError("Invalid Questions File", "The questions file exists but is not formatted correctly.\n\n"
					+ "Please fix or replace the file before managing questions.");

			// Lock all modification actions
			view.addBtn.setDisable(true);
			view.editBtn.setDisable(true);
			view.deleteBtn.setDisable(true);
		}

		case EMPTY, HAS_DATA -> {
			// No popup needed; state is reflected in the table UI
		}
		}

		setupHandlers();
	}

	private void setupHandlers() {

		// Navigate back to the main menu
		view.backBtn.setOnAction(e -> {
			Main.showMainMenu(primaryStage);
		});

		// Enable Edit/Delete only when a table row is selected
		view.table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

			boolean hasSelection = (newSelection != null);
			view.editBtn.setDisable(!hasSelection);
			view.deleteBtn.setDisable(!hasSelection);
		});

		// Add a new question
		view.addBtn.setOnAction(e -> {
			QuestionFormDialog dialog = new QuestionFormDialog("Add New Question", null);

			Optional<Question> result = dialog.showAndWait();

			result.ifPresent(q -> {
				boolean success = SysData.addQuestion(q);

				if (success) {
					showInfo("Question Added", "The question was added successfully.");
					refreshQuestions();
				} else {
					showError("Add Failed",
							"Unable to save the question.\n\n" + "The questions file may be open in another program.\n"
									+ "Please close it and try again.");
				}
			});
		});

		// Edit the selected question
		view.editBtn.setOnAction(e -> {
			Question selected = view.table.getSelectionModel().getSelectedItem();
			if (selected == null)
				return;

			QuestionFormDialog dialog = new QuestionFormDialog("Edit Question", selected);

			Optional<Question> result = dialog.showAndWait();

			result.ifPresent(q -> {
				boolean success = SysData.updateQuestion(q);

				if (success) {
					showInfo("Question Updated", "The question was updated successfully.");
					refreshQuestions();
				} else {
					showError("Update Failed",
							"Unable to update the question.\n\n"
									+ "The questions file may be open in another program.\n"
									+ "Please close it and try again.");
				}
			});
		});

		// Delete the selected question (with confirmation)
		view.deleteBtn.setOnAction(e -> {
			Question selected = view.table.getSelectionModel().getSelectedItem();
			if (selected == null)
				return;

			DeleteQuestionConfirmDialog dialog = new DeleteQuestionConfirmDialog(selected);

			Optional<Boolean> result = dialog.showAndWait();
			if (result.isEmpty() || !result.get())
				return;

			boolean success = SysData.deleteQuestion(selected);

			if (success) {
				showInfo("Question Deleted", "The question was deleted successfully.");
				refreshQuestions();
			} else {
				showError("Delete Failed", "Unable to delete the question.\n\n"
						+ "The questions file may be open in another program.\n" + "Please close it and try again.");
			}
		});
	}

	// Reloads questions after add/edit/delete operations
	private void refreshQuestions() {
		questionList.setAll(SysData.loadQuestions());
	}

	// Shows a simple information dialog
	private void showInfo(String title, String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
				javafx.scene.control.Alert.AlertType.INFORMATION);

		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		// allow text wrapping + auto height
		alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
		alert.getDialogPane().setMinWidth(500);

		alert.showAndWait();
	}

	// Shows a simple error dialog
	private void showError(String title, String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);

		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
		alert.getDialogPane().setMinWidth(500);

		alert.showAndWait();
	}

}
