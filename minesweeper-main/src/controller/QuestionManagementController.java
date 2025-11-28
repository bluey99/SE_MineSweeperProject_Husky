package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Question;
import model.QuestionRepository;
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

        List<Question> loaded = QuestionRepository.loadQuestions();
        this.questionList = FXCollections.observableArrayList(loaded);
        view.table.setItems(questionList);

        setupHandlers();
    }

    private void setupHandlers() {
        view.backBtn.setOnAction(e -> {
            // Go back to main menu
            Main.showMainMenu(primaryStage);
        });

        view.addBtn.setOnAction(e -> {
            QuestionFormDialog dialog = new QuestionFormDialog("Add New Question", null);
            Optional<Question> result = dialog.showAndWait();
            result.ifPresent(q -> {
                // Save to CSV and refresh
                QuestionRepository.addQuestion(q);
                refreshFromRepository();
            });
        });

        view.editBtn.setOnAction(e -> {
            Question selected = view.table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            QuestionFormDialog dialog = new QuestionFormDialog("Edit Question", selected);
            Optional<Question> result = dialog.showAndWait();
            result.ifPresent(q -> {
                QuestionRepository.updateQuestion(q);
                refreshFromRepository();
            });
        });

        view.deleteBtn.setOnAction(e -> {
            Question selected = view.table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            QuestionRepository.deleteQuestion(selected);
            refreshFromRepository();
        });
    }

    private void refreshFromRepository() {
        questionList.setAll(QuestionRepository.loadQuestions());
    }

    public Scene createScene() {
        return new Scene(view, 900, 600);
    }
}
