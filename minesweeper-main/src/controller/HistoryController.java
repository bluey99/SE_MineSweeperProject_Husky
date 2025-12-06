package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.GameHistoryEntry;
import model.SysData;
import view.HistoryView;

import java.util.List;

public class HistoryController {

    private final Stage primaryStage;
    public final HistoryView view;
    private final ObservableList<GameHistoryEntry> historyList;

    public HistoryController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.view = new HistoryView();

        // Load history from SysData (Model)
        List<GameHistoryEntry> loaded = SysData.loadHistory();
        this.historyList = FXCollections.observableArrayList(loaded);
        view.table.setItems(historyList);

        setupHandlers();
    }

    private void setupHandlers() {
        view.backBtn.setOnAction(e -> {
            Main.showMainMenu(primaryStage);
        });
    }

    public Scene createScene(double width, double height) {
        return new Scene(view, width, height);
    }
}
