package controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import view.Menu;
import view.SetupView;
// import view.HistoryView;   // ❌ no longer needed
// ✅ use HistoryController instead
// (HistoryController is in the same package `controller`, so no extra import needed)

public class Main extends Application {

    private static Stage primaryStage;   // used so other screens can change the scene

    // Preferred menu size (will be clamped to screen)
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
        // Clear any old size constraints from the game screen
        stage.setMinWidth(0);
        stage.setMaxWidth(Double.MAX_VALUE);
        stage.setMinHeight(0);
        stage.setMaxHeight(Double.MAX_VALUE);
        stage.setResizable(false);

        Menu menu = new Menu();

        // Clamp menu size to screen
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double maxWidth = bounds.getWidth() - 80;
        double maxHeight = bounds.getHeight() - 80;

        double width = Math.min(MENU_WIDTH, maxWidth);
        double height = Math.min(MENU_HEIGHT, maxHeight);

        Scene menuScene = new Scene(menu, width, height);
        stage.setScene(menuScene);

        // Fix menu size
        stage.setMinWidth(width);
        stage.setMaxWidth(width);
        stage.setMinHeight(height);
        stage.setMaxHeight(height);

        stage.centerOnScreen();
        stage.show();

        // ----------------- BUTTON HANDLERS -----------------

        // Start game -> go to setup screen
        menu.startBtn.setOnAction(e -> {
            SetupView setup = new SetupView(new Main()); // Main used as callback
            Scene setupScene = new Scene(setup, width, height);
            stage.setScene(setupScene);
            stage.centerOnScreen();
        });

        // History button -> open History screen via controller (MVC)
        menu.historyBtn.setOnAction(e -> {
            HistoryController historyController = new HistoryController(stage);
            Scene historyScene = historyController.createScene(width, height);
            stage.setScene(historyScene);

            // keep the History window at menu size
            stage.setMinWidth(width);
            stage.setMaxWidth(width);
            stage.setMinHeight(height);
            stage.setMaxHeight(height);
            stage.centerOnScreen();
        });

        // Question Management
        menu.questionManagementBtn.setOnAction(e -> {
            QuestionManagementController qm = new QuestionManagementController(stage);
            Scene qmScene = qm.createScene();
            stage.setScene(qmScene);

            // keep the QM window at menu size
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
        // pass primaryStage into controller so it can return to menu
        GameController controller = new GameController(difficulty, p1, p2, primaryStage);

        // Get full usable screen area (excludes taskbar)
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double screenX = bounds.getMinX();
        double screenY = bounds.getMinY();
        double screenW = bounds.getWidth();
        double screenH = bounds.getHeight();

        // Remove menu size limits so we can expand
        primaryStage.setMinWidth(0);
        primaryStage.setMaxWidth(Double.MAX_VALUE);
        primaryStage.setMinHeight(0);
        primaryStage.setMaxHeight(Double.MAX_VALUE);

        // Create scene that matches the screen size
        Scene gameScene = new Scene(controller.gameView, screenW, screenH);
        primaryStage.setScene(gameScene);

        // Position window at top-left and stretch to full visible screen
        primaryStage.setX(screenX);
        primaryStage.setY(screenY);
        primaryStage.setWidth(screenW);
        primaryStage.setHeight(screenH);

        // Lock size so layout stays stable
        primaryStage.setMinWidth(screenW);
        primaryStage.setMaxWidth(screenW);
        primaryStage.setMinHeight(screenH);
        primaryStage.setMaxHeight(screenH);

        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
