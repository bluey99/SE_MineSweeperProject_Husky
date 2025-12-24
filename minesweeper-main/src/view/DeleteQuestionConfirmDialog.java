package view;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Question;

import java.util.Optional;

/**
 * Confirmation dialog for deleting a question.
 * Presents a clear warning and requires explicit user approval
 * before performing a destructive action.
 */
public class DeleteQuestionConfirmDialog {

    // Dialog returns true only if the user confirms deletion
    private final Dialog<Boolean> dialog = new Dialog<>();

    public DeleteQuestionConfirmDialog(Question q) {

        dialog.setTitle("Delete Question");
        dialog.setHeaderText(null);
        dialog.setResizable(false);

        // Define dialog actions
        ButtonType deleteType = new ButtonType(
                "Delete",
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelType = new ButtonType(
                "Cancel",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        dialog.getDialogPane().getButtonTypes().addAll(deleteType, cancelType);

        // Dialog content layout
        VBox root = new VBox(12);
        root.setPadding(new Insets(18));
        // Use default dialog styling for consistency

        Label title = new Label("Delete Question?");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Show question text to avoid accidental deletion
        Label msg = new Label(
                "Are you sure you want to delete this question?\n\n" +
                "\"" + q.getText() + "\"\n\n" +
                "This action cannot be undone."
        );
        msg.setWrapText(true);
        msg.setStyle("-fx-font-size: 14px;");

        root.getChildren().addAll(title, msg);

        dialog.getDialogPane().setContent(root);

        // Convert button choice to boolean result
        dialog.setResultConverter(btn ->
                btn.getButtonData() == ButtonBar.ButtonData.OK_DONE
        );
    }

    // Displays the dialog and returns the user's decision
    public Optional<Boolean> showAndWait() {
        return dialog.showAndWait();
    }
}
