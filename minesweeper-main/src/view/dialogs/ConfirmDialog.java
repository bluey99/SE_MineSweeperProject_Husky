package view.dialogs;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

public class ConfirmDialog extends AbstractDialogTemplate {

    private final String title;
    private final String message;

    public ConfirmDialog(String title, String message) {
        this.title = title;
        this.message = message;
    }

    @Override protected String getTitle() { return title; }
    @Override protected String getMessage() { return message; }

    @Override
    protected void configureButtons() {
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.CANCEL,
                ButtonType.OK
        );
    }

    @Override
    protected void styleButtons() {
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button cancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);

        ok.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;");
        cancel.setStyle("-fx-background-color: #1F2937; -fx-text-fill: #E5E7EB;");
    }
}
