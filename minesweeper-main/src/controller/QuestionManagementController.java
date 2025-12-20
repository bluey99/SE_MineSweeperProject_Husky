package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Question;
import model.SysData;
import view.DeleteQuestionConfirmDialog;
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

        // Load questions via SysData (model)
        List<Question> loaded = SysData.loadQuestions();
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
                SysData.addQuestion(q);
                refreshQuestions();
            });
        });

        view.editBtn.setOnAction(e -> {
            Question selected = view.table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            QuestionFormDialog dialog = new QuestionFormDialog("Edit Question", selected);
            Optional<Question> result = dialog.showAndWait();
            result.ifPresent(q -> {
                SysData.updateQuestion(q);
                refreshQuestions();
            });
        });

        view.deleteBtn.setOnAction(e -> {
            Question selected = view.table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            DeleteQuestionConfirmDialog dialog =
                    new DeleteQuestionConfirmDialog(selected);

            Optional<Boolean> result = dialog.showAndWait();
            if (result.isEmpty() || !result.get()) return;

            SysData.deleteQuestion(selected);
            refreshQuestions();
        });



    }

    private void refreshQuestions() {
        questionList.setAll(SysData.loadQuestions());
    }

    public Scene createScene() {
        return new Scene(view, 900, 600);
    }
    
   

}
