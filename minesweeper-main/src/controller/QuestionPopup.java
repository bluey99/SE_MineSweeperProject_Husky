package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Question;

import java.util.Optional;

public class QuestionPopup {

    public static Optional<Boolean> show(Stage owner, Question q, String qDiffLabel) {

        final Boolean[] result = new Boolean[]{null};

        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);

        VBox root = new VBox(16);
        root.setPadding(new Insets(22));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(460);

        root.setStyle("""
            -fx-background-color: #020617;
            -fx-background-radius: 18;
            -fx-border-radius: 18;
            -fx-border-color: #1E3A8A;
            -fx-border-width: 2;
        """);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Question Cell");
        title.setStyle("""
            -fx-text-fill: #93C5FD;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
        """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("✕");
        close.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #93C5FD;
            -fx-font-size: 14px;
            -fx-cursor: hand;
        """);
        close.setOnAction(e -> stage.close());

        header.getChildren().addAll(title, spacer, close);

        // Difficulty tag
        Label diffLabel = new Label(qDiffLabel + " Question");
        diffLabel.setPadding(new Insets(4, 12, 4, 12));
        diffLabel.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-background-radius: 12;
            -fx-background-color: %s;
        """.formatted(getDifficultyColor(qDiffLabel)));

        // Question text
        Label questionLbl = new Label(q.getText());
        questionLbl.setWrapText(true);
        questionLbl.setMaxWidth(410);
        questionLbl.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 16px;
        """);

        VBox answersBox = new VBox(10);
        answersBox.setAlignment(Pos.CENTER);

        String[] options = q.getOptions();
        if (options == null || options.length < 4) {
            options = new String[]{"A", "B", "C", "D"};
        }

        int correctIdx = q.getCorrectIndex();

        for (int i = 0; i < 4; i++) {
            final int idx = i;

            Button btn = new Button((char)('A' + i) + ") " + options[i]);
            btn.setPrefWidth(360);
            btn.setPrefHeight(44);

            btn.setStyle("""
                -fx-background-color: #020617;
                -fx-border-color: #2563EB;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-cursor: hand;
            """);

            btn.setOnMouseEntered(e ->
                    btn.setStyle(btn.getStyle() + "-fx-background-color: #1E3A8A;")
            );
            btn.setOnMouseExited(e ->
                    btn.setStyle(btn.getStyle() + "-fx-background-color: #020617;")
            );

            btn.setOnAction(e -> {
                result[0] = (idx == correctIdx);
                stage.close();
            });

            answersBox.getChildren().add(btn);
        }

        root.getChildren().addAll(header, diffLabel, questionLbl, answersBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        stage.showAndWait();
        return Optional.ofNullable(result[0]);
    }

    private static String getDifficultyColor(String diff) {
        return switch (diff) {
            case "Intermediate" -> "#D97706";
            case "Hard" -> "#DC2626";
            case "Expert" -> "#7C3AED";
            default -> "#059669"; // Easy
        };
    }
}
