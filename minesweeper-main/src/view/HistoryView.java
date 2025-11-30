package view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.GameHistoryEntry;
import model.SysData;

public class HistoryView {

    public static void show(Stage owner) {
        Stage stage = new Stage();
        stage.setTitle("Game History");

        // ====== TOP BAR: Back button + Title/subtitle ======
        Button backBtn = new Button("← Back to Menu");
        backBtn.setOnAction(e -> stage.close());
        backBtn.setFocusTraversable(false);
        backBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #e5e7eb;" +          // light gray
                "-fx-font-size: 14px;"
        );

        Label title = new Label("Game History");
        title.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle = new Label("Review previous cooperative games");
        subtitle.setStyle(
                "-fx-text-fill: #9ca3af;" +          // gray-400
                "-fx-font-size: 14px;"
        );

        VBox titleBox = new VBox(4, title, subtitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(16, backBtn, titleBox, spacer);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 16, 8, 16));

        // ====== TABLE ======
        TableView<GameHistoryEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFocusTraversable(false);

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

        // make Result look like colored tags (similar to difficulty chips)
        resultCol.setCellFactory(col -> new TableCell<>() {
            private final Label tag = new Label();

            {
                tag.setPadding(new Insets(2, 10, 2, 10));
                tag.setStyle(
                        "-fx-background-radius: 999;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
                );
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String upper = item.toUpperCase();
                    if (upper.equals("WIN")) {
                        tag.setText("Win");
                        tag.setStyle(
                                "-fx-background-color: #16a34a;" + // green
                                "-fx-background-radius: 999;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;"
                        );
                    } else {
                        tag.setText("Lose");
                        tag.setStyle(
                                "-fx-background-color: #dc2626;" + // red
                                "-fx-background-radius: 999;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;"
                        );
                    }
                    setGraphic(tag);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<GameHistoryEntry, Number> scoreCol = new TableColumn<>("Final Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("finalScore"));

        TableColumn<GameHistoryEntry, Number> lengthCol = new TableColumn<>("Game Length (sec)");
        lengthCol.setCellValueFactory(new PropertyValueFactory<>("gameLengthSeconds"));

        table.getColumns().addAll(
                dateCol, diffCol, p1Col, p2Col, resultCol, scoreCol, lengthCol
        );

        table.setItems(FXCollections.observableArrayList(SysData.loadHistory()));

        // Optional bottom padding area, like in Question Management
        VBox root = new VBox(10, topBar, table);
        root.setPadding(new Insets(0, 16, 16, 16));
        root.setStyle("-fx-background-color: #020617;"); // dark navy (similar vibe)

        Scene scene = new Scene(root, 900, 500);
        stage.setScene(scene);
        stage.initOwner(owner);
        stage.initModality(Modality.NONE);
        stage.show();
    }
}
