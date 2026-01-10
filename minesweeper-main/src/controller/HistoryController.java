package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import model.GameHistoryEntry;
import model.SysData;
import view.HistoryView;

import java.util.List;
import java.util.Optional;

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

        view.backBtn.setOnAction(e -> Main.showMainMenu(primaryStage));

        view.clearHistoryBtn.setOnAction(e -> {
            if (!confirm("Clear History", "Delete all history records?")) return;
            SysData.clearHistory();
            historyList.clear();
            refreshState();
            success("History cleared.");
        });

        // -------------------- TRIM HISTORY (RESTORED) --------------------
        view.trimHistoryBtn.setOnAction(e -> {

            if (historyList.isEmpty()) {
                error("No history available.");
                return;
            }

            int keepK = view.showTrimHistoryDialog(); // -1 if cancelled
            if (keepK <= 0) return;

            int currentSize = historyList.size();

            if (keepK >= currentSize) {
                error("You already have only " + currentSize + " games.\nNothing to remove.");
                return;
            }

            if (!confirm(
                    "Trim History",
                    "Keep only the most recent " + keepK + " games?\n" +
                            (currentSize - keepK) + " older entries will be removed."
            )) return;

            int removed = SysData.trimHistory(keepK);
            reloadHistory();
            success("Kept last " + keepK + " games. Removed " + removed + " older entries.");
            refreshState();
        });
        // ---------------------------------------------------------------

        view.table.getSelectionModel().selectedItemProperty().addListener((obs, o, selected) -> {
            boolean hasSelection = selected != null;
            view.deleteSelectedBtn.setDisable(!hasSelection);

            if (!hasSelection) {
                Tooltip.install(view.deleteBtnWrapper, deleteDisabledTooltip);
            } else {
                Tooltip.uninstall(view.deleteBtnWrapper, deleteDisabledTooltip);
            }
        });

        view.deleteSelectedBtn.setOnAction(e -> {
            GameHistoryEntry selected = view.table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            if (!confirm("Delete Selected", "Delete the selected history entry?")) return;

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

    private boolean confirm(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    private void success(String msg) {
        view.statusLabel.setText(msg);
        view.statusLabel.setStyle("-fx-text-fill: #22C55E;");
    }

    private void error(String msg) {
        view.statusLabel.setText(msg);
        view.statusLabel.setStyle("-fx-text-fill: #F87171;");
    }

    public Scene createScene(double w, double h) {
        return new Scene(view, w, h);
    }
}
