package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class StatisticsView extends BorderPane {

    public final Button backBtn = new Button("Menu");
    private final Label emptyLabel = new Label("No statistics yet.\nPlay some games first.");

    // Overall
    private final Label gamesPlayedVal = new Label("-");
    private final Label winRatioVal = new Label("-");

    // Easy
    private final Label easyWinsVal = new Label("-");
    private final Label easyBestTimeVal = new Label("-");
    private final Label easyBestScoreVal = new Label("-");

    // Medium
    private final Label medWinsVal = new Label("-");
    private final Label medBestTimeVal = new Label("-");
    private final Label medBestScoreVal = new Label("-");

    // Hard
    private final Label hardWinsVal = new Label("-");
    private final Label hardBestTimeVal = new Label("-");
    private final Label hardBestScoreVal = new Label("-");

    private final VBox content = new VBox(12);

    public StatisticsView() {
        buildUI();
    }

    private void buildUI() {
        setStyle("-fx-background-color: #0f172a;");

        // ===== TOP BAR =====
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

        Label iconLabel = new Label("📊");
        iconLabel.setTextFill(Color.web("#22C55E"));
        iconLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));

        VBox titleBox = new VBox(3);
        Label title = new Label("Statistics");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label subtitle = new Label("Overall + by difficulty");
        subtitle.setTextFill(Color.web("#9CA3AF"));
        subtitle.setFont(Font.font("Arial", 13));

        titleBox.getChildren().addAll(title, subtitle);

        HBox headerLeft = new HBox(16, backBtn, iconLabel, titleBox);
        headerLeft.setAlignment(Pos.CENTER_LEFT);

        HBox settingsBar = TopBarFactory.createTopBar();
        settingsBar.setPadding(Insets.EMPTY);

        BorderPane header = new BorderPane();
        header.setPadding(new Insets(18, 24, 8, 24));
        header.setLeft(headerLeft);
        header.setRight(settingsBar);
        setTop(header);

        // ===== CENTER CONTENT (compact, fits screen) =====
        content.setPadding(new Insets(8, 24, 18, 24));
        content.setAlignment(Pos.TOP_CENTER);

        // Row: Overall cards
        HBox overallRow = new HBox(12,
                bigCard("Games Played", gamesPlayedVal),
                bigCard("Win Ratio", winRatioVal)
        );
        overallRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(overallRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(overallRow.getChildren().get(1), Priority.ALWAYS);

        // Difficulty rows (each one compact)
        VBox easyBlock = difficultyBlock("Easy", easyWinsVal, easyBestTimeVal, easyBestScoreVal);
        VBox medBlock = difficultyBlock("Medium", medWinsVal, medBestTimeVal, medBestScoreVal);
        VBox hardBlock = difficultyBlock("Hard", hardWinsVal, hardBestTimeVal, hardBestScoreVal);

        emptyLabel.setTextFill(Color.web("#9CA3AF"));
        emptyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        emptyLabel.setAlignment(Pos.CENTER);
        emptyLabel.setVisible(false);

        content.getChildren().addAll(overallRow, easyBlock, medBlock, hardBlock, emptyLabel);
        setCenter(content);
    }

    private VBox bigCard(String nameText, Label valueLabel) {
        VBox card = new VBox(6);

        Label name = new Label(nameText);
        name.setTextFill(Color.web("#9CA3AF"));
        name.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        valueLabel.setTextFill(Color.WHITE);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));

        card.getChildren().addAll(name, valueLabel);
        card.setPadding(new Insets(12));
        card.setStyle(cardStyle());
        card.setMinHeight(78);

        return card;
    }

    private VBox difficultyBlock(String diffName, Label wins, Label bestTime, Label bestScore) {
        VBox block = new VBox(8);
        block.setPadding(new Insets(12));
        block.setStyle(cardStyle());

        Label title = new Label(diffName);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        HBox row = new HBox(10,
                miniStat("Wins", wins),
                miniStat("Best Time", bestTime),
                miniStat("Best Score", bestScore)
        );
        row.setAlignment(Pos.CENTER);
        for (int i = 0; i < row.getChildren().size(); i++) {
            HBox.setHgrow(row.getChildren().get(i), Priority.ALWAYS);
        }

        block.getChildren().addAll(title, row);
        return block;
    }

    private VBox miniStat(String labelText, Label valueLabel) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10));
        box.setStyle(
                "-fx-background-color: rgba(2,6,23,0.35);" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(148,163,184,0.12);" +
                "-fx-border-radius: 12;"
        );

        Label label = new Label(labelText);
        label.setTextFill(Color.web("#9CA3AF"));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        valueLabel.setTextFill(Color.WHITE);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        box.getChildren().addAll(label, valueLabel);
        return box;
    }

    private String cardStyle() {
        return "-fx-background-color: rgba(15,23,42,0.85);" +
               "-fx-background-radius: 14;" +
               "-fx-border-color: rgba(148,163,184,0.15);" +
               "-fx-border-radius: 14;";
    }

    public void setEmptyState(boolean empty) {
        // hide everything except the empty label
        for (int i = 0; i < content.getChildren().size(); i++) {
            if (content.getChildren().get(i) != emptyLabel) {
                content.getChildren().get(i).setVisible(!empty);
                content.getChildren().get(i).setManaged(!empty);
            }
        }
        emptyLabel.setVisible(empty);
        emptyLabel.setManaged(empty);
    }

    public void setOverall(String gamesPlayed, String winRatio) {
        gamesPlayedVal.setText(gamesPlayed);
        winRatioVal.setText(winRatio);
    }

    public void setDifficultyStats(
            int easyWins, String easyBestTime, String easyBestScore,
            int medWins, String medBestTime, String medBestScore,
            int hardWins, String hardBestTime, String hardBestScore
    ) {
        easyWinsVal.setText(String.valueOf(easyWins));
        easyBestTimeVal.setText(easyBestTime);
        easyBestScoreVal.setText(easyBestScore);

        medWinsVal.setText(String.valueOf(medWins));
        medBestTimeVal.setText(medBestTime);
        medBestScoreVal.setText(medBestScore);

        hardWinsVal.setText(String.valueOf(hardWins));
        hardBestTimeVal.setText(hardBestTime);
        hardBestScoreVal.setText(hardBestScore);
    }
}
