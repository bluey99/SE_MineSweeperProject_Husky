package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.GameHistoryEntry;

public class HistoryView extends BorderPane {

    public final Button backBtn = new Button("Menu");

    // Actions
    public final Button clearHistoryBtn = new Button("Clear History");
    public final TextField keepRecentField = new TextField();
    public final Button keepRecentBtn = new Button("Keep Recent");
    public final Button deleteSelectedBtn = new Button("Delete Selected");

    // NEW: user hint + status + empty label
    public final Label keepHintLabel = new Label("Keep K = number of most recent games to keep (older entries will be removed).");
    public final Label statusLabel = new Label("");
    public final Label emptyLabel = new Label("No history yet.");

    public final TableView<GameHistoryEntry> table = new TableView<>();

    public HistoryView() {
        buildUI();
    }

    private void buildUI() {
        setStyle("-fx-background-color: #0B1220;");

        // ---------- Header ----------
        Label title = new Label("Game History");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 44));

        Label subtitle = new Label("Review previous cooperative games");
        subtitle.setTextFill(Color.web("#9CA3AF"));
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        VBox titleBox = new VBox(6, title, subtitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        backBtn.setStyle("""
            -fx-background-color: rgba(255,255,255,0.08);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 10 22;
            -fx-background-radius: 18;
        """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(18, backBtn, spacer, titleBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(26, 26, 8, 26));

        // ---------- Action bar ----------
        keepRecentField.setPromptText("K (e.g. 5)");
        keepRecentField.setPrefWidth(90);

        // NEW: tooltip explains Keep K
        keepRecentField.setTooltip(new Tooltip(
                "Keep only the last K games.\nOlder history entries will be removed."
        ));

        clearHistoryBtn.setStyle("""
            -fx-background-color: rgba(255,255,255,0.10);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 8 14;
            -fx-background-radius: 14;
        """);

        keepRecentBtn.setStyle("""
            -fx-background-color: rgba(255,255,255,0.10);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 8 14;
            -fx-background-radius: 14;
        """);

        deleteSelectedBtn.setStyle("""
            -fx-background-color: rgba(239,68,68,0.85);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 8 14;
            -fx-background-radius: 14;
        """);

        Label keepLbl = new Label("Keep:");
        keepLbl.setTextFill(Color.web("#D1D5DB"));

        HBox actions = new HBox(10, clearHistoryBtn, keepLbl, keepRecentField, keepRecentBtn, deleteSelectedBtn);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(0, 26, 10, 26));

        // NEW: hint label style
        keepHintLabel.setTextFill(Color.web("#9CA3AF"));
        keepHintLabel.setStyle("-fx-font-size: 12px;");
        keepHintLabel.setPadding(new Insets(0, 26, 6, 26));

        statusLabel.setTextFill(Color.web("#93C5FD"));
        statusLabel.setPadding(new Insets(0, 26, 8, 26));

        // ---------- Table ----------
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(560);

        TableColumn<GameHistoryEntry, String> cDate = new TableColumn<>("Date & Time");
        cDate.setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        TableColumn<GameHistoryEntry, String> cDiff = new TableColumn<>("Difficulty");
        cDiff.setCellValueFactory(new PropertyValueFactory<>("difficulty"));

        TableColumn<GameHistoryEntry, String> cP1 = new TableColumn<>("Player 1");
        cP1.setCellValueFactory(new PropertyValueFactory<>("player1Name"));

        TableColumn<GameHistoryEntry, String> cP2 = new TableColumn<>("Player 2");
        cP2.setCellValueFactory(new PropertyValueFactory<>("player2Name"));

        TableColumn<GameHistoryEntry, String> cRes = new TableColumn<>("Result");
        cRes.setCellValueFactory(new PropertyValueFactory<>("result"));

        cRes.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }

                Label pill = new Label(item);
                pill.setStyle("""
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-padding: 4 14;
                    -fx-background-radius: 16;
                """);
                if ("WIN".equalsIgnoreCase(item)) pill.setStyle(pill.getStyle() + "-fx-background-color: #16A34A;");
                else pill.setStyle(pill.getStyle() + "-fx-background-color: #DC2626;");

                setGraphic(pill);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<GameHistoryEntry, Integer> cScore = new TableColumn<>("Final Score");
        cScore.setCellValueFactory(new PropertyValueFactory<>("finalScore"));

        TableColumn<GameHistoryEntry, Integer> cTime = new TableColumn<>("Game Length (sec)");
        cTime.setCellValueFactory(new PropertyValueFactory<>("gameLengthSeconds"));

        table.getColumns().addAll(cDate, cDiff, cP1, cP2, cRes, cScore, cTime);

        emptyLabel.setTextFill(Color.web("#9CA3AF"));
        emptyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);
        VBox.setMargin(emptyLabel, new Insets(8, 0, 0, 0));

        VBox center = new VBox(0, actions, keepHintLabel, statusLabel, table, emptyLabel);
        center.setPadding(new Insets(0, 0, 26, 0));

        setTop(header);
        setCenter(center);
    }
}
