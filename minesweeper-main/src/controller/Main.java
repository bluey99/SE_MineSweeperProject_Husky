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

    // Holds the currently running Main instance so static callers can delegate
    private static Main instance = null;

    // Preferred menu size (will be clamped to screen)
    private static final double MENU_WIDTH = 900;
    private static final double MENU_HEIGHT = 650;

    @Override
    public void start(Stage stage) {
        instance = this;            // remember the running instance
        primaryStage = stage;
        primaryStage.setTitle("Cooperative Minesweeper");

        // Use the instance method through the wrapper (works either way)
        showMainMenu(primaryStage);

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Static wrapper so older code can keep calling Main.showMainMenu(stage).
     * It simply delegates to the running Main instance.
     */
    public static void showMainMenu(Stage stage) {
        if (instance != null) {
            instance.showMainMenuInstance(stage);
        } else {
            // Fallback: if instance not set (shouldn't happen in normal run), create a temporary instance
            // and call the instance method (defensive, but typically unnecessary).
            new Main().showMainMenuInstance(stage);
        }
    }

    /**
     * Actual instance implementation of showing the main menu.
     * Kept as an instance method to allow 'this' to be passed into views (SetupView).
     */
    private void showMainMenuInstance(Stage stage) {
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
        // resize window so it exactly fits the new menu scene
        stage.sizeToScene();

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
            // pass the current Main instance (this) so SetupView can call back
            SetupView setup = new SetupView(this);
            Scene setupScene = new Scene(setup, width, height);
            stage.setScene(setupScene);
            // resize stage to fit setup scene content
            stage.sizeToScene();

            // keep the Setup window at menu size (consistent with previous behavior)
            stage.setMinWidth(width);
            stage.setMaxWidth(width);
            stage.setMinHeight(height);
            stage.setMaxHeight(height);
            stage.centerOnScreen();
        });

        // History button -> open History screen via controller (MVC)
        menu.historyBtn.setOnAction(e -> {
            HistoryController historyController = new HistoryController(stage);
            Scene historyScene = historyController.createScene(width, height);
            stage.setScene(historyScene);
            // resize stage to fit history scene
            stage.sizeToScene();

            // keep the History window at menu size
            stage.setMinWidth(width);
            stage.setMaxWidth(width);
            stage.setMinHeight(height);
            stage.setMaxHeight(height);
            stage.centerOnScreen();
<<<<<<< Updated upstream
=======

            // Back button inside HistoryView -> return to menu (call instance wrapper)
            historyView.backBtn.setOnAction(ev -> showMainMenu(stage));
>>>>>>> Stashed changes
        });

        // Question Management
        menu.questionManagementBtn.setOnAction(e -> {
            QuestionManagementController qm = new QuestionManagementController(stage);
            Scene qmScene = qm.createScene();
            stage.setScene(qmScene);
            // resize stage to fit QM scene
            stage.sizeToScene();

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
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
