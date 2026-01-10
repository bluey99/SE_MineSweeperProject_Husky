package view.dialogs;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

public class ErrorDialog extends AbstractDialogTemplate {

    private final String title;
    private final String message;

    public ErrorDialog(String title, String message) {
        this.title = title;
        this.message = message;
    }

    @Override protected String getTitle() { return title; }
    @Override protected String getMessage() { return message; }

    @Override
    protected void configureButtons() {
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
    }

    @Override
    protected void styleButtons() {
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white;");
    }
}
