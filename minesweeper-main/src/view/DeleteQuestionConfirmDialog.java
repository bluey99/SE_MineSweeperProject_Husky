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

public class DeleteQuestionConfirmDialog {

    private final Dialog<Boolean> dialog = new Dialog<>();

    public DeleteQuestionConfirmDialog(Question q) {

        dialog.setTitle("Delete Question");
        dialog.setHeaderText(null);
        dialog.setResizable(false);

        ButtonType deleteType = new ButtonType(
                "Delete",
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelType = new ButtonType(
                "Cancel",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        dialog.getDialogPane().getButtonTypes().addAll(deleteType, cancelType);

        VBox root = new VBox(12);
        root.setPadding(new Insets(18));
        // ✅ NO background color → use native light theme

        Label title = new Label("Delete Question?");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        // default text color (black)

        Label msg = new Label(
                "Are you sure you want to delete this question?\n\n" +
                "\"" + q.getText() + "\"\n\n" +
                "This action cannot be undone."
        );
        msg.setWrapText(true);
        msg.setStyle("-fx-font-size: 14px;");

        root.getChildren().addAll(title, msg);

        dialog.getDialogPane().setContent(root);
        // ✅ NO dialog pane styling

        dialog.setResultConverter(btn ->
                btn.getButtonData() == ButtonBar.ButtonData.OK_DONE
        );
    }

    public Optional<Boolean> showAndWait() {
        return dialog.showAndWait();
    }
}
