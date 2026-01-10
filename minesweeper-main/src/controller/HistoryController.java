package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import model.GameHistoryEntry;
import model.SysData;
import view.HistoryView;
import view.dialogs.ConfirmDialog;
import view.dialogs.ErrorDialog;
import view.dialogs.InfoDialog;

import java.util.List;

/**
 * HistoryController
 *
 * Controls the behavior of the Game History screen.
 * Handles user interactions, validation, persistence,
 * and synchronization between the view and the data layer.
 */
public class HistoryController {

    private final Stage primaryStage;
    private final HistoryView view;
    private final ObservableList<GameHistoryEntry> historyList;

    private final Tooltip deleteDisabledTooltip =
            new Tooltip("Select a row to delete");

    public HistoryController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.view = new HistoryView();

        List<GameHistoryEntry> loaded = SysData.loadHistory();
        loaded.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));

        this.historyList = FXCollections.observableArrayList(loaded);
        view.table.setItems(historyList);

        setupHandlers();
        refreshState();
    }

    /**
     * Registers all UI event handlers.
     */
    private void setupHandlers() {

        // Back to main menu
        view.backBtn.setOnAction(e ->
                Main.showMainMenu(primaryStage)
        );

        // -------------------- CLEAR HISTORY --------------------
        view.clearHistoryBtn.setOnAction(e -> {

            ConfirmDialog dialog = new ConfirmDialog(
                    "Clear History",
                    "Delete all history records?"
            );

            if (dialog.show() != ButtonType.OK) return;

            SysData.clearHistory();
            historyList.clear();
            refreshState();
            success("History cleared.");
        });

        // -------------------- TRIM HISTORY --------------------
        view.trimHistoryBtn.setOnAction(e -> {

            if (historyList.isEmpty()) {
                new ErrorDialog(
                        "Invalid Action",
                        "No history available."
                ).show();
                return;
            }

            int keepK = view.showTrimHistoryDialog(); // -1 if cancelled
            if (keepK <= 0) return;

            int currentSize = historyList.size();

            if (keepK >= currentSize) {
                new ErrorDialog(
                        "Invalid Action",
                        "You already have only " + currentSize + " games.\nNothing to remove."
                ).show();
                return;
            }

            ConfirmDialog confirm = new ConfirmDialog(
                    "Trim History",
                    "Keep only the most recent " + keepK + " games?\n" +
                    (currentSize - keepK) + " older entries will be removed."
            );

            if (confirm.show() != ButtonType.OK) return;

            int removed = SysData.trimHistory(keepK);
            reloadHistory();
            refreshState();

            success("Kept last " + keepK + " games. Removed " + removed + " older entries.");
        });

        // -------------------- TABLE SELECTION --------------------
        view.table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            view.deleteSelectedBtn.setDisable(!hasSelection);

            if (!hasSelection) {
                Tooltip.install(view.deleteBtnWrapper, deleteDisabledTooltip);
            } else {
                Tooltip.uninstall(view.deleteBtnWrapper, deleteDisabledTooltip);
            }
        });

        // -------------------- DELETE SELECTED --------------------
        view.deleteSelectedBtn.setOnAction(e -> {

            GameHistoryEntry selected =
                    view.table.getSelectionModel().getSelectedItem();

            if (selected == null) return;

            ConfirmDialog confirm = new ConfirmDialog(
                    "Delete Selected",
                    "Delete the selected history entry?"
            );

            if (confirm.show() != ButtonType.OK) return;

            SysData.deleteHistoryEntry(selected);
            historyList.remove(selected);
            refreshState();
            success("Entry deleted.");
        });
    }

    /**
     * Reloads history data from persistent storage.
     */
    private void reloadHistory() {
        List<GameHistoryEntry> loaded = SysData.loadHistory();
        loaded.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));
        historyList.setAll(loaded);
    }

    /**
     * Updates UI state based on data availability and selection.
     */
    private void refreshState() {
        boolean empty = historyList.isEmpty();

        view.emptyLabel.setVisible(empty);
        view.clearHistoryBtn.setDisable(empty);
        view.trimHistoryBtn.setDisable(empty);
        view.deleteSelectedBtn.setDisable(true);

        Tooltip.install(view.deleteBtnWrapper, deleteDisabledTooltip);
    }

    /**
     * Displays a success message in the status label.
     */
    private void success(String msg) {
        view.statusLabel.setText(msg);
        view.statusLabel.setStyle("-fx-text-fill: #22C55E;");
    }

    public Scene createScene(double width, double height) {
        return new Scene(view, width, height);
    }
}
