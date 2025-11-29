package controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import view.Menu;
import view.SetupView;

public class Main extends Application {

    private static Stage primaryStage;   // used so other screens can change the scene

    private static final double MENU_WIDTH = 900;
    private static final double MENU_HEIGHT = 650;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Cooperative Minesweeper");

        showMainMenu(primaryStage);

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

    // Called from Start + Back button
    public static void showMainMenu(Stage stage) {
        Menu menu = new Menu();

        // ניקח את גודל המסך ונוסיף מרווח
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double maxWidth = bounds.getWidth() - 80;   // קצת מרווח מהקצוות
        double maxHeight = bounds.getHeight() - 80;

        double width = Math.min(MENU_WIDTH, maxWidth);
        double height = Math.min(MENU_HEIGHT, maxHeight);

        Scene menuScene = new Scene(menu, width, height);
        stage.setScene(menuScene);

        // ננעל על הגודל הזה
        stage.setResizable(false);
        stage.setMinWidth(width);
        stage.setMaxWidth(width);
        stage.setMinHeight(height);
        stage.setMaxHeight(height);

        stage.centerOnScreen();
        stage.show();

        // Start game -> go to setup screen
        menu.startBtn.setOnAction(e -> {
            SetupView setup = new SetupView(new Main()); // Main is only used as callback
            Scene setupScene = new Scene(setup, width, height);
            stage.setScene(setupScene);
            stage.centerOnScreen();
        });

        // History button (placeholder)
        menu.historyBtn.setOnAction(e -> {
            System.out.println("History clicked");
        });

        // Question Management
        menu.questionManagementBtn.setOnAction(e -> {
            QuestionManagementController qm = new QuestionManagementController(stage);
            Scene qmScene = qm.createScene();
            stage.setScene(qmScene);

            stage.setMinWidth(width);
            stage.setMaxWidth(width);
            stage.setMinHeight(height);
            stage.setMaxHeight(height);
            stage.centerOnScreen();
        });
    }

    /**
     * Called from SetupView after validation is done.
     */
    public void startGameFromSetup(String p1, String p2, String difficulty) {
        GameController controller = new GameController(difficulty, p1, p2);

        // גדלים "רצויים" לכל רמה (רק כנקודת פתיחה)
        double desiredWidth;
        double desiredHeight;

        switch (difficulty) {
            case "Easy":
                desiredWidth = 1150;
                desiredHeight = 700;
                break;
            case "Medium":
                desiredWidth = 1350;
                desiredHeight = 800;
                break;
            case "Hard":
            default:
                desiredWidth = 1500;
                desiredHeight = 880;
                break;
        }

        // נוודא שלא עובר את המסך
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double maxWidth = bounds.getWidth() - 40;   // מרווח קטן מהקצה
        double maxHeight = bounds.getHeight() - 80; // מרווח מלמעלה/טוסטר בר

        double width = Math.min(desiredWidth, maxWidth);
        double height = Math.min(desiredHeight, maxHeight);

        Scene gameScene = new Scene(controller.gameView, width, height);
        primaryStage.setScene(gameScene);

        primaryStage.setResizable(false);
        primaryStage.setMinWidth(width);
        primaryStage.setMaxWidth(width);
        primaryStage.setMinHeight(height);
        primaryStage.setMaxHeight(height);

        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
