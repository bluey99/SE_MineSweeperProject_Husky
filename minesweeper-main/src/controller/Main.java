package controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main entry point for Cooperative Minesweeper
 * Shows setup screen for player names and difficulty selection
 */
public class Main extends Application {
    
    private String player1Name = "Player 1";
    private String player2Name = "Player 2";
    private String selectedDifficulty = "Easy";
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Cooperative Minesweeper");
        
        // Show setup screen first
        Scene setupScene = createSetupScene(primaryStage);
        primaryStage.setScene(setupScene);
        primaryStage.show();
        
        // Handle window close
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }
    
    private Scene createSetupScene(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        
        // Gradient background
        BackgroundFill bgFill1 = new BackgroundFill(
            Color.web("#E3F2FD"), 
            CornerRadii.EMPTY, 
            Insets.EMPTY
        );
        root.setBackground(new Background(bgFill1));
        
        // Title
        Label titleLabel = new Label("⚑ Cooperative Minesweeper ⚑");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        titleLabel.setTextFill(Color.web("#1565C0"));
        
        Label subtitleLabel = new Label("Two players • One goal • Shared score & lives");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.web("#424242"));
        
        // Info card
        VBox infoCard = new VBox(10);
        infoCard.setAlignment(Pos.CENTER);
        infoCard.setPadding(new Insets(20));
        infoCard.setMaxWidth(500);
        infoCard.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
        );
        
        // Player 1 name input
        Label p1Label = new Label("Player 1 Name:");
        p1Label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        TextField p1Field = new TextField(player1Name);
        p1Field.setPrefWidth(300);
        p1Field.setFont(Font.font(14));
        p1Field.setStyle(
            "-fx-background-radius: 6;" +
            "-fx-border-color: #BDBDBD;" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 2;"
        );
        
        // Player 2 name input
        Label p2Label = new Label("Player 2 Name:");
        p2Label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        TextField p2Field = new TextField(player2Name);
        p2Field.setPrefWidth(300);
        p2Field.setFont(Font.font(14));
        p2Field.setStyle(
            "-fx-background-radius: 6;" +
            "-fx-border-color: #BDBDBD;" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 2;"
        );
        
        // Difficulty selection
        Label diffLabel = new Label("Select Difficulty:");
        diffLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        ToggleGroup difficultyGroup = new ToggleGroup();
        
        RadioButton easyRadio = new RadioButton("Easy");
        easyRadio.setToggleGroup(difficultyGroup);
        easyRadio.setSelected(true);
        easyRadio.setFont(Font.font(13));
        
        RadioButton mediumRadio = new RadioButton("Medium");
        mediumRadio.setToggleGroup(difficultyGroup);
        mediumRadio.setFont(Font.font(13));
        
        RadioButton hardRadio = new RadioButton("Hard");
        hardRadio.setToggleGroup(difficultyGroup);
        hardRadio.setFont(Font.font(13));
        
        HBox difficultyBox = new HBox(20);
        difficultyBox.setAlignment(Pos.CENTER);
        difficultyBox.getChildren().addAll(easyRadio, mediumRadio, hardRadio);
        
        // Difficulty info display
        Label diffInfoLabel = new Label(
            "Easy: 9×9 grid • 10 mines per board • 10 shared lives\n" +
            "Medium: 13×13 grid • 26 mines per board • 8 shared lives\n" +
            "Hard: 16×16 grid • 44 mines per board • 6 shared lives"
        );
        diffInfoLabel.setFont(Font.font("Arial", 12));
        diffInfoLabel.setTextFill(Color.web("#616161"));
        diffInfoLabel.setStyle("-fx-text-alignment: center;");
        diffInfoLabel.setWrapText(true);
        diffInfoLabel.setMaxWidth(450);
        
        // Update info when difficulty changes
        difficultyGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == easyRadio) {
                diffInfoLabel.setText(
                    "Easy: 9×9 grid • 10 mines per board • 10 shared lives\n" +
                    "Perfect for beginners and learning the game!"
                );
            } else if (newVal == mediumRadio) {
                diffInfoLabel.setText(
                    "Medium: 13×13 grid • 26 mines per board • 8 shared lives\n" +
                    "Balanced challenge for experienced players."
                );
            } else if (newVal == hardRadio) {
                diffInfoLabel.setText(
                    "Hard: 16×16 grid • 44 mines per board • 6 shared lives\n" +
                    "Maximum challenge! Requires excellent teamwork."
                );
            }
        });
        
        infoCard.getChildren().addAll(
            p1Label, p1Field,
            new Label(""),
            p2Label, p2Field,
            new Label(""),
            diffLabel, difficultyBox,
            new Label(""),
            diffInfoLabel
        );
        
        // Start button
        Button startBtn = new Button("▶  START GAME");
        startBtn.setPrefSize(250, 55);
        startBtn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        startBtn.setStyle(
            "-fx-background-color: #4CAF50;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);"
        );
        
        startBtn.setOnMouseEntered(e -> 
            startBtn.setStyle(
                "-fx-background-color: #45A049;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 4);" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
            )
        );
        
        startBtn.setOnMouseExited(e -> 
            startBtn.setStyle(
                "-fx-background-color: #4CAF50;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
            )
        );
        
        startBtn.setOnAction(e -> {
            // 1. Validate first
            if (!validatePlayerNames(p1Field, p2Field)) {
                // Validation failed so do not start game
                return;
            }

            // 2. If valid, save names
            player1Name = p1Field.getText().trim();
            player2Name = p2Field.getText().trim();

            // 3. Read difficulty
            RadioButton selected = (RadioButton) difficultyGroup.getSelectedToggle();
            selectedDifficulty = selected.getText();

            // 4. Start game
            startGame(primaryStage);
        });

        
        // Footer
        Label footerLabel = new Label("Manage questions • View history • Save scores (Coming soon)");
        footerLabel.setFont(Font.font("Arial", 10));
        footerLabel.setTextFill(Color.web("#9E9E9E"));
        
        root.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            new Label(""),
            infoCard,
            new Label(""),
            startBtn,
            new Label(""),
            footerLabel
        );
        
        return new Scene(root, 600, 750);
    }
    
    
    private boolean validatePlayerNames(TextField p1Field, TextField p2Field) {
        String p1 = p1Field.getText().trim();
        String p2 = p2Field.getText().trim();

        StringBuilder errorMsg = new StringBuilder();

        // 1. Not empty
        if (p1.isEmpty()) {
            errorMsg.append("- Player 1 name cannot be empty.\n");
        }
        if (p2.isEmpty()) {
            errorMsg.append("- Player 2 name cannot be empty.\n");
        }

        // 2. Max length (you can change 12 to another number)
        if (!p1.isEmpty() && p1.length() > 12) {
            errorMsg.append("- Player 1 name must be at most 12 characters.\n");
        }
        if (!p2.isEmpty() && p2.length() > 12) {
            errorMsg.append("- Player 2 name must be at most 12 characters.\n");
        }

        // 3. Names must be different
        if (!p1.isEmpty() && !p2.isEmpty() && p1.equalsIgnoreCase(p2)) {
            errorMsg.append("- Players must have different names.\n");
        }

        // If there are any errors → show alert and return false
        if (errorMsg.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Player Names");
            alert.setHeaderText("Please fix the following:");
            alert.setContentText(errorMsg.toString());
            alert.showAndWait();
            return false;
        }

        return true; // all good
    }

    
    private void startGame(Stage primaryStage) {
        GameController controller = new GameController(selectedDifficulty, player1Name, player2Name);
        
        // Adjust window size based on difficulty
        int width = 900, height = 650;
        switch(selectedDifficulty) {
            case "Easy":
                width = 950;
                height = 650;
                break;
            case "Medium":
                width = 1150;
                height = 800;
                break;
            case "Hard":
                width = 1350;
                height = 900;
                break;
        }
        
        Scene gameScene = new Scene(controller.gameView, width, height);
        primaryStage.setScene(gameScene);
        primaryStage.centerOnScreen();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
