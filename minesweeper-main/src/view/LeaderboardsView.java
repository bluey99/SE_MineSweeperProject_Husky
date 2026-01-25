package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.LeaderboardEntry;

public class LeaderboardsView extends BorderPane {

    public final Button backBtn = new Button("Menu");
    public final Label emptyLabel = new Label("No leaderboard data yet.");
    public final TableView<LeaderboardEntry> table = new TableView<>();

    public LeaderboardsView() {
        buildUI();
    }

    private void buildUI() {
        setStyle("-fx-background-color: #0f172a;");

        // =========================
        // TOP BAR
        // =========================
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

        Label iconLabel = new Label("🏆");
        iconLabel.setTextFill(Color.web("#F59E0B"));
        iconLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        VBox titleBox = new VBox(5);
        Label title = new Label("Leaderboards");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));

        Label subtitle = new Label("Top duos by wins");
        subtitle.setTextFill(Color.web("#9CA3AF"));
        subtitle.setFont(Font.font("Arial", 14));

        titleBox.getChildren().addAll(title, subtitle);

        HBox headerLeft = new HBox(20, backBtn, iconLabel, titleBox);
        headerLeft.setAlignment(Pos.CENTER_LEFT);

        HBox settingsBar = TopBarFactory.createTopBar();
        settingsBar.setPadding(Insets.EMPTY);

        BorderPane header = new BorderPane();
        header.setPadding(new Insets(20, 30, 10, 30));
        header.setLeft(headerLeft);
        header.setRight(settingsBar);

        setTop(header);

        // =========================
        // TABLE
        // =========================
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No leaderboard data yet."));

        TableColumn<LeaderboardEntry, Integer> cRank = new TableColumn<>("#");
        cRank.setCellValueFactory(new PropertyValueFactory<>("rank"));

        TableColumn<LeaderboardEntry, String> cDuo = new TableColumn<>("Duo");
        cDuo.setCellValueFactory(new PropertyValueFactory<>("duo"));

        TableColumn<LeaderboardEntry, Integer> cWins = new TableColumn<>("Wins");
        cWins.setCellValueFactory(new PropertyValueFactory<>("wins"));

        TableColumn<LeaderboardEntry, Integer> cGames = new TableColumn<>("Games");
        cGames.setCellValueFactory(new PropertyValueFactory<>("games"));

        TableColumn<LeaderboardEntry, String> cRate = new TableColumn<>("Win %");
        cRate.setCellValueFactory(new PropertyValueFactory<>("winRate"));

        table.getColumns().setAll(cRank, cDuo, cWins, cGames, cRate);

        emptyLabel.setTextFill(Color.web("#9CA3AF"));
        emptyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        emptyLabel.setVisible(false);

        VBox centerBox = new VBox(10, table, emptyLabel);
        centerBox.setPadding(new Insets(10, 30, 20, 30));

        setCenter(centerBox);
    }

    public void setEmptyState(boolean empty) {
        table.setVisible(!empty);
        table.setManaged(!empty);

        emptyLabel.setVisible(empty);
        emptyLabel.setManaged(empty);
    }
}
