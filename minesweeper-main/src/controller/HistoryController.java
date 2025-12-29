package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import model.GameHistoryEntry;
import model.SysData;
import view.HistoryView;

import java.util.List;
import java.util.Optional;

public class HistoryController {

    private final Stage primaryStage;
    private final HistoryView view;
    private final ObservableList<GameHistoryEntry> historyList;

    public HistoryController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.view = new HistoryView();

        List<GameHistoryEntry> loaded = SysData.loadHistory();
        loaded.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));

        this.historyList = FXCollections.observableArrayList(loaded);
        view.table.setItems(historyList);

        setupHandlers();
        refreshEmptyState();
    }

    private void setupHandlers() {
        view.backBtn.setOnAction(e -> Main.showMainMenu(primaryStage));

        view.clearHistoryBtn.setOnAction(e -> {
            if (historyList.isEmpty()) {
                error("History is already empty.");
                return;
            }

            if (!confirm("Clear History", "Are you sure you want to delete ALL history?")) return;

            int removed = SysData.clearHistory();
            historyList.clear();
            success("History cleared (" + removed + " records).");
            refreshEmptyState();
        });

        // UPDATED: Keep Recent with full exceptions/validation
        view.keepRecentBtn.setOnAction(e -> {

            if (historyList.isEmpty()) {
                error("No history available.");
                return;
            }

            String txt = view.keepRecentField.getText().trim();
            int keepK;

            try {
                keepK = Integer.parseInt(txt);
            } catch (NumberFormatException ex) {
                error("Please enter a valid number for K (e.g. 5).");
                return;
            }

            if (keepK < 0) {
                error("K must be 0 or greater.");
                return;
            }

            int currentSize = historyList.size();

            if (keepK >= currentSize) {
                error("You already have only " + currentSize + " games.\nNothing to remove.");
                return;
            }

            if (!confirm(
                    "Keep Recent Games",
                    "Keep only the most recent " + keepK + " games?\n" +
                            (currentSize - keepK) + " older entries will be removed."
            )) return;

            int removed = SysData.trimHistory(keepK);
            reloadHistory();
            success("Kept last " + keepK + " games. Removed " + removed + " older entries.");
            refreshEmptyState();
        });

        view.deleteSelectedBtn.setOnAction(e -> {
            GameHistoryEntry selected = view.table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                error("Please select a row first.");
                return;
            }

            if (!confirm("Delete Selected", "Delete the selected history row?")) return;

            boolean ok = SysData.deleteHistoryEntry(selected);
            if (!ok) {
                error("Failed to delete selected row.");
                return;
            }

            historyList.remove(selected);
            success("Selected row deleted.");
            refreshEmptyState();
        });
    }

    private void reloadHistory() {
        List<GameHistoryEntry> loaded = SysData.loadHistory();
        loaded.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));
        historyList.setAll(loaded);
    }

    private void refreshEmptyState() {
        boolean empty = historyList.isEmpty();

        view.emptyLabel.setVisible(empty);
        view.emptyLabel.setManaged(empty);

        view.clearHistoryBtn.setDisable(empty);
        view.keepRecentBtn.setDisable(empty);
        view.deleteSelectedBtn.setDisable(empty);
    }

    private boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }

    private void success(String msg) {
        view.statusLabel.setText(msg);
        view.statusLabel.setStyle("-fx-text-fill: #22C55E; -fx-font-weight: bold;");
    }

    private void error(String msg) {
        view.statusLabel.setText(msg);
        view.statusLabel.setStyle("-fx-text-fill: #F87171; -fx-font-weight: bold;");
    }

    public Scene createScene(double width, double height) {
        return new Scene(view, width, height);
    }
}
