package controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import view.Menu;
import view.SetupView;
import view.HistoryView;

public class Main extends Application {

    // used so other screens can change the scene
    private static Stage primaryStage;

    // Preferred menu size (will be clamped to screen)
    private static final double MENU_WIDTH = 900;
    private static final double MENU_HEIGHT = 650;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Cooperative Minesweeper");

<<<<<<< Updated upstream
=======
        // Use the instance method through the wrapper
>>>>>>> Stashed changes
        showMainMenu(primaryStage);

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

<<<<<<< Updated upstream
    // Called from Start + Back button
    public static void showMainMenu(Stage stage) {
=======
    // --- STATIC HELPERS ------------------------------------------------------

    /**
     * Static wrapper so older code can keep calling Main.showMainMenu(stage).
     * It simply delegates to the running Main instance.
     */
    public static void showMainMenu(Stage stage) {
        if (instance != null) {
            instance.showMainMenuInstance(stage);
        } else {
            new Main().showMainMenuInstance(stage);
        }
    }

    /** Static getter so views can access the primary stage (e.g. SetupView). */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    // --- INSTANCE IMPLEMENTATION ---------------------------------------------

    /**
     * Actual instance implementation of showing the main menu.
     * Kept as an instance method to allow 'this' to be passed into views (SetupView).
     */
    private void showMainMenuInstance(Stage stage) {
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======
        stage.sizeToScene();  // make window fit menu
>>>>>>> Stashed changes

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
<<<<<<< Updated upstream
            SetupView setup = new SetupView(new Main()); // Main used as callback
            Scene setupScene = new Scene(setup, width, height);
            stage.setScene(setupScene);
=======
            SetupView setup = new SetupView(this);
            Scene setupScene = new Scene(setup, width, height);
            stage.setScene(setupScene);
            stage.sizeToScene();

            // keep the Setup window at menu size
            stage.setMinWidth(width);
            stage.setMaxWidth(width);
            stage.setMinHeight(height);
            stage.setMaxHeight(height);
>>>>>>> Stashed changes
            stage.centerOnScreen();
        });

        // History button -> open HistoryView
        menu.historyBtn.setOnAction(e -> {
            HistoryView historyView = new HistoryView();
            Scene historyScene = new Scene(historyView, width, height);
            stage.setScene(historyScene);
<<<<<<< Updated upstream
=======
            stage.sizeToScene();
>>>>>>> Stashed changes

            // keep the History window at menu size
            stage.setMinWidth(width);
            stage.setMaxWidth(width);
            stage.setMinHeight(height);
            stage.setMaxHeight(height);
            stage.centerOnScreen();
<<<<<<< Updated upstream
=======

            // Back button inside HistoryView -> return to menu
            historyView.backBtn.setOnAction(ev -> Main.showMainMenu(stage));
>>>>>>> Stashed changes
        });

        // Question Management
        menu.questionManagementBtn.setOnAction(e -> {
            QuestionManagementController qm = new QuestionManagementController(stage);
            Scene qmScene = qm.createScene();
            stage.setScene(qmScene);
<<<<<<< Updated upstream
=======
            stage.sizeToScene();
>>>>>>> Stashed changes

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
