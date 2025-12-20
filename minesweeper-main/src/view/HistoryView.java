package view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.GameHistoryEntry;
import model.SysData;

public class HistoryView extends BorderPane {

    public final Button backBtn = new Button("Menu");
    public final TableView<GameHistoryEntry> table = new TableView<>();

    public HistoryView() {

        // ==== ROOT STYLE (same as QuestionManagementView) ====
        setStyle("-fx-background-color: #0f172a;");

        // ==== TOP BAR ====
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(20, 30, 10, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);

        // ==== MENU BUTTON (text-only) ====
        backBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        String normalStyle = """
            -fx-background-color: #1e293b;
            -fx-text-fill: #e5e7eb;
            -fx-background-radius: 999;
            -fx-padding: 7 18 7 18;
            -fx-cursor: hand;
        """;

        String hoverStyle = """
            -fx-background-color: #334155;
            -fx-text-fill: #ffffff;
            -fx-background-radius: 999;
            -fx-padding: 7 18 7 18;
            -fx-cursor: hand;
        """;

        backBtn.setStyle(normalStyle);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(hoverStyle));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(normalStyle));

        // Title icon (scroll icon for history)
        Label icon = new Label();
        icon.setTextFill(Color.web("#FBBF24"));
        icon.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        VBox titleBox = new VBox(5);
        Label title = new Label("Game History");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));

        Label subtitle = new Label("Review previous cooperative games");
        subtitle.setTextFill(Color.web("#9CA3AF"));
        subtitle.setFont(Font.font("Arial", 14));

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, icon, titleBox, spacer);
        setTop(topBar);

        // ==== TABLE ====
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFocusTraversable(false);
        table.setPlaceholder(new Label("No history records yet."));

        TableColumn<GameHistoryEntry, String> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        TableColumn<GameHistoryEntry, String> diffCol = new TableColumn<>("Difficulty");
        diffCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));

        TableColumn<GameHistoryEntry, String> p1Col = new TableColumn<>("Player 1");
        p1Col.setCellValueFactory(new PropertyValueFactory<>("player1Name"));

        TableColumn<GameHistoryEntry, String> p2Col = new TableColumn<>("Player 2");
        p2Col.setCellValueFactory(new PropertyValueFactory<>("player2Name"));

        TableColumn<GameHistoryEntry, String> resultCol = new TableColumn<>("Result");
        resultCol.setCellValueFactory(new PropertyValueFactory<>("result"));
        resultCol.setCellFactory(col -> new TableCell<>() {
            private final Label tag = new Label();

            {
                tag.setPadding(new Insets(2, 10, 2, 10));
                tag.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                tag.setTextFill(Color.WHITE);
                tag.setStyle("-fx-background-radius: 999;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item.equalsIgnoreCase("WIN"))
                        tag.setStyle("-fx-background-color: #16a34a; -fx-background-radius: 999;");
                    else
                        tag.setStyle("-fx-background-color: #dc2626; -fx-background-radius: 999;");

                    tag.setText(item);
                    setGraphic(tag);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<GameHistoryEntry, Number> scoreCol = new TableColumn<>("Final Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("finalScore"));

        TableColumn<GameHistoryEntry, Number> lengthCol = new TableColumn<>("Game Length (sec)");
        lengthCol.setCellValueFactory(new PropertyValueFactory<>("gameLengthSeconds"));

        table.getColumns().addAll(dateCol, diffCol, p1Col, p2Col, resultCol, scoreCol, lengthCol);

        VBox centerBox = new VBox(10, table);
        centerBox.setPadding(new Insets(10, 30, 10, 30));
        setCenter(centerBox);

        // === Load history from CSV via SysData ===
        loadHistoryData(dateCol);
    }

    private void loadHistoryData(TableColumn<GameHistoryEntry, String> dateCol) {
        var historyList = SysData.loadHistory();

        if (historyList != null && !historyList.isEmpty()) {
            table.setItems(FXCollections.observableArrayList(historyList));

            table.getSortOrder().clear();
            dateCol.setSortType(TableColumn.SortType.DESCENDING);
            table.getSortOrder().add(dateCol);
            table.sort();
        }
    }
}
