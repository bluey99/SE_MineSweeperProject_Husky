package view.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * TEMPLATE METHOD
 * Defines the dialog creation algorithm.
 */
public abstract class AbstractDialogTemplate {

    protected Dialog<ButtonType> dialog;

    // -------- TEMPLATE METHOD --------
    public final ButtonType show() {
        createDialog();
        buildContent();
        configureButtons();
        styleButtons();
        return dialog.showAndWait().orElse(ButtonType.CANCEL);
    }

    // -------- FIXED STEPS --------
    private void createDialog() {
        dialog = new Dialog<>();
        dialog.setHeaderText(null);
    }

    private void buildContent() {
        Label titleLbl = new Label(getTitle());
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label msgLbl = new Label(getMessage());
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-text-fill: #9CA3AF;");

        VBox box = new VBox(10, titleLbl, msgLbl);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: #0f172a;");

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #0f172a;" +
                "-fx-border-color: #1E293B;" +
                "-fx-border-radius: 10;"
        );
    }

    // -------- HOOKS --------
    protected abstract String getTitle();
    protected abstract String getMessage();
    protected abstract void configureButtons();
    protected abstract void styleButtons();
}
