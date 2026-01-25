// controller/Main.java
package controller;

import javafx.application.Application;
import controller.StatisticsController;

import service.SoundService;
import view.Theme;
import controller.LeaderboardsController;


import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;          // ✅ ADDED
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.QuestionsFileStatus;
import model.SysData;
import view.Menu;
import view.MultiplayerSetupView;
import view.SetupView;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import service.LocalGameState;
import service.LocalResumeCodec;

public class Main extends Application {

    // Used so other screens can change the scene
    private static Stage primaryStage;

    // Hold the running Main instance so we can use instance methods from static calls
    private static Main instance = null;
    
    private static boolean resumePromptShown = false;


    // Preferred menu size (will be clamped to screen)
    private static final double MENU_WIDTH = 900;
    private static final double MENU_HEIGHT = 650;

    @Override
    public void start(Stage stage) {
        instance = this;
        primaryStage = stage;
        primaryStage.setTitle("Cooperative Minesweeper");

        // start background music on app launch
        SoundService.playMenuMusic();
        // preload UI hover/click sounds
        SoundService.initUiSounds();
      
        // Show the main menu
        showMainMenu(primaryStage);
        
        Platform.runLater(this::maybeOfferResumeLocalGame);


        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }


    // --- STATIC HELPERS ------------------------------------------------------

    /** Static wrapper: other classes call this to return to the main menu. */
    public static void showMainMenu(Stage stage) {
    	 SoundService.playMenuMusic();
        if (instance != null) {
            instance.showMainMenuInstance(stage);
        }
    }

    /** Static getter so views can access the primary stage (e.g. SetupView). */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /** ✅ NEW: go to Setup screen from anywhere (Menu button uses it). */
    public static void showSetup(Stage stage) {
        if (instance != null) {
            instance.showSetupInstance(stage);
        }
    }

    /** ✅ NEW: go to History screen from anywhere (Menu button uses it). */
    public static void showHistory(Stage stage) {
        if (instance != null) {
            instance.showHistoryInstance(stage);
        }
    }
    
    /** ✅ NEW: go to Statistics screen from anywhere (TopBar button uses it). */
    public static void showStatistics(Stage stage) {
        if (instance != null) {
            instance.showStatisticsInstance(stage);
        }
    }
    
    public static void showLeaderboards(Stage stage) {
        if (instance != null) {
            instance.showLeaderboardsInstance(stage);
        }
    }



    /** ✅ NEW: go to Question Management from anywhere (Menu button uses it). */
    public static void showQuestionManagement(Stage stage) {
        if (instance != null) {
            instance.showQuestionManagementInstance(stage);
        }
    }

    // --- INSTANCE IMPLEMENTATION ---------------------------------------------

    /** Utility: clamp menu/setup/history sizes to screen and return width/height. */
    private double[] getClampedMenuSize() {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double maxWidth = bounds.getWidth() - 80;
        double maxHeight = bounds.getHeight() - 80;

        double width = Math.min(MENU_WIDTH, maxWidth);
        double height = Math.min(MENU_HEIGHT, maxHeight);

        return new double[]{width, height};
    }

    /** Utility: apply fixed window size (menu-sized) */
    private void applyFixedWindowSize(Stage stage, double width, double height) {
        stage.setMinWidth(width);
        stage.setMaxWidth(width);
        stage.setMinHeight(height);
        stage.setMaxHeight(height);
        stage.setResizable(false);
        stage.centerOnScreen();
    }

    private void showMainMenuInstance(Stage stage) {
    	
    	SoundService.playMenuMusic();
        // ✅ RESET stage state from the game
        stage.setFullScreen(false);
        stage.setMaximized(false);

        stage.setMinWidth(0);
        stage.setMinHeight(0);
        stage.setMaxWidth(Double.MAX_VALUE);
        stage.setMaxHeight(Double.MAX_VALUE);
        stage.setResizable(true);   // allow resizing while switching scenes

        Menu menu = new Menu();
        
        SoundService.applyUiSoundsWhenReady(menu);

        double[] size = getClampedMenuSize();
        double width = size[0];
        double height = size[1];

        Scene menuScene = new Scene(menu, width, height);
        menuScene.setFill(Color.BLACK);
        stage.setScene(menuScene);
        stage.sizeToScene();

        applyFixedWindowSize(stage, width, height); // this will lock it again
        stage.show();

        menu.startBtn.setOnAction(e -> Main.showSetup(stage));
        menu.historyBtn.setOnAction(e -> Main.showHistory(stage));
        menu.questionManagementBtn.setOnAction(e -> Main.showQuestionManagement(stage));
    }


    private void showSetupInstance(Stage stage) {
        double[] size = getClampedMenuSize();
        double width = size[0];
        double height = size[1];

        SetupView setup = new SetupView(this);
        SoundService.applyUiSoundsWhenReady(setup);
        Scene setupScene = new Scene(setup, width, height);
        setupScene.setFill(Color.BLACK);             // ✅ ADDED
        stage.setScene(setupScene);
        stage.sizeToScene();

        applyFixedWindowSize(stage, width, height);
    }

    private void showHistoryInstance(Stage stage) {
        double[] size = getClampedMenuSize();
        double width = size[0];
        double height = size[1];

        HistoryController hc = new HistoryController(stage);
        Scene historyScene = hc.createScene(width, height);
        historyScene.setFill(Color.BLACK);           // ✅ ADDED
        SoundService.applyUiSoundsWhenReady(historyScene.getRoot());
        stage.setScene(historyScene);
        stage.sizeToScene();

        applyFixedWindowSize(stage, width, height);
    }
    
    private void showStatisticsInstance(Stage stage) {
        double[] size = getClampedMenuSize();
        double width = size[0];
        double height = size[1];

        StatisticsController sc = new StatisticsController(stage);
        Scene statsScene = sc.createScene(width, height);
        statsScene.setFill(Color.BLACK);

        SoundService.applyUiSoundsWhenReady(statsScene.getRoot());
        stage.setScene(statsScene);
        stage.sizeToScene();

        applyFixedWindowSize(stage, width, height);
    }
    
    private void showLeaderboardsInstance(Stage stage) {
        double[] size = getClampedMenuSize();
        double width = size[0];
        double height = size[1];

        LeaderboardsController lc = new LeaderboardsController(stage);
        Scene scene = lc.createScene(width, height);
        scene.setFill(Color.BLACK);

        SoundService.applyUiSoundsWhenReady(scene.getRoot());
        stage.setScene(scene);
        stage.sizeToScene();

        applyFixedWindowSize(stage, width, height);
    }



    private void showQuestionManagementInstance(Stage stage) {
        double[] size = getClampedMenuSize();
        double width = size[0];
        double height = size[1];

        QuestionsFileStatus status = SysData.getQuestionsFileStatus();

        if (status == QuestionsFileStatus.MALFORMED) {
            showQuestionsFileError(stage, width, height);
            return; // do NOT navigate
        }

        QuestionManagementController qm = new QuestionManagementController(stage);
        Scene qmScene = new Scene(qm.view, width, height);
        qmScene.setFill(Color.BLACK);                // ✅ ADDED
        SoundService.applyUiSoundsWhenReady(qm.view);
        stage.setScene(qmScene);
        stage.sizeToScene();
        applyFixedWindowSize(stage, width, height);
    }
    

    /**
     * Called from SetupView after validation is done.
     */

    public void startGameFromSetup(String p1, String p2, String difficulty) {
    	
        SysData.deleteLocalResumePayload();

    	SoundService.playGameMusicForDifficulty(difficulty);

        // ✅ RESET stage state from menu
        primaryStage.setFullScreen(false);
        primaryStage.setMaximized(false);

        primaryStage.setMinWidth(0);
        primaryStage.setMinHeight(0);
        primaryStage.setMaxWidth(Double.MAX_VALUE);
        primaryStage.setMaxHeight(Double.MAX_VALUE);
        primaryStage.setResizable(true);

        // ✅ create fresh game
        GameController.resetInstance();
        GameController controller =
                GameController.getInstance(difficulty, p1, p2, primaryStage);

        Scene gameScene = new Scene(controller.gameView);
        gameScene.setFill(Color.BLACK);

        gameScene.getStylesheets().add(
                Main.class.getResource("/css/game.css").toExternalForm()
        );

        primaryStage.setScene(gameScene);

        // ✅ maximize AFTER scene is set (taskbar stays visible)
        Platform.runLater(() -> {
            primaryStage.setMaximized(true);
            primaryStage.centerOnScreen();
        });
    }


    // ✅ NEW OVERLOAD: start game with deterministic seed (multiplayer)
    public GameController startGameFromSetupReturnController(String p1, String p2, String difficulty, long seed) {
        
        
        primaryStage.setFullScreen(false);
        primaryStage.setMaximized(false);
        
        primaryStage.setMinWidth(0);
        primaryStage.setMaxWidth(Double.MAX_VALUE);
        primaryStage.setMinHeight(0);
        primaryStage.setMaxHeight(Double.MAX_VALUE);
        primaryStage.setResizable(true);

        GameController.resetInstance();
        GameController controller = GameController.getInstance(difficulty, p1, p2, primaryStage, seed);
        
        Scene gameScene = new Scene(controller.gameView);
        gameScene.setFill(Color.BLACK);
        gameScene.getStylesheets().add(
                Main.class.getResource("/css/game.css").toExternalForm()
        );
        primaryStage.setScene(gameScene);
        
     // ✅ maximize AFTER scene is set (taskbar stays visible)
        Platform.runLater(() -> {
            primaryStage.setMaximized(true);
            primaryStage.centerOnScreen();
        });

        return controller;
    }

    // Displays a blocking dialog when the questions file is malformed
    // Allows the user to either fix the file manually or recreate it automatically
    private void showQuestionsFileError(Stage stage, double width, double height) {

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);

        alert.setTitle("Invalid Questions File");
        alert.setHeaderText(null);
        alert.setContentText("The questions file is not formatted correctly.\n\n"
                + "You can fix the file manually, or recreate a clean file automatically.");

        // Define available user actions
        javafx.scene.control.ButtonType fixManually = new javafx.scene.control.ButtonType("Fix Manually");
        javafx.scene.control.ButtonType recreate = new javafx.scene.control.ButtonType("Recreate File");

        alert.getButtonTypes().setAll(fixManually, recreate);

        // Ensure dialog resizes correctly to content
        alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(520);

        alert.showAndWait().ifPresent(choice -> {

            // User chose to recreate the file automatically
            if (choice == recreate) {

                boolean success = SysData.recreateQuestionsFile();

                if (success) {
                    // Recreated successfully → allow access to Question Management
                    QuestionManagementController qm = new QuestionManagementController(stage);

                    Scene qmScene = new Scene(qm.view, width, height);
                    qmScene.setFill(Color.BLACK);     // ✅ ADDED
                    stage.setScene(qmScene);
                    stage.sizeToScene();
                    stage.centerOnScreen();
                } else {
                    // Recreate failed (file likely locked)
                    showFileLockedError();
                }
            }

            // Fix manually → remain on main menu
        });
    }

    // Displays an error dialog when the questions file cannot be modified
    // Usually occurs when the file is open or locked by another application
    private void showFileLockedError() {

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);

        alert.setTitle("File In Use");
        alert.setHeaderText(null);
        alert.setContentText("The questions file is currently open in another program.\n\n"
                + "Please close the file and try again.");

        // Allow dialog to resize based on message length
        alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(500);

        alert.showAndWait();
    }

    public static void showMultiplayerSetup(Stage stage) {
        if (instance != null) {
            instance.showMultiplayerSetupInstance(stage);
        }
    }

    private void showMultiplayerSetupInstance(Stage stage) {
        double[] size = getClampedMenuSize();
        double width = size[0];
        double height = size[1];

        MultiplayerSetupView mpView = new MultiplayerSetupView(this);
        Scene scene = new Scene(mpView, width, height);
        scene.setFill(Color.BLACK);                  // ✅ ADDED
        SoundService.applyUiSoundsWhenReady(mpView);
        stage.setScene(scene);
        stage.sizeToScene();
        applyFixedWindowSize(stage, width, height);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    private void maybeOfferResumeLocalGame() {
        if (resumePromptShown) return;
        resumePromptShown = true;

        if (!SysData.hasLocalResumePayload()) return;

        Optional<String> payloadOpt = SysData.loadLocalResumePayload();
        if (!payloadOpt.isPresent()) {
            SysData.deleteLocalResumePayload();
            return;
        }

        String payload = payloadOpt.get();
        Optional<LocalGameState> stOpt = LocalResumeCodec.decode(payload);
        if (!stOpt.isPresent()) {
            SysData.deleteLocalResumePayload();
            return;
        }

        LocalGameState st = stOpt.get();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Continue Game?");
        alert.setHeaderText("A local game is still in progress.");
        alert.setContentText("Do you want to continue where you left off?");

        ButtonType btnContinue = new ButtonType("Continue");
        ButtonType btnNew = new ButtonType("Start New Game");
        alert.getButtonTypes().setAll(btnContinue, btnNew);

        Optional<ButtonType> res = alert.showAndWait();
        if (!res.isPresent()) return;

        if (res.get() == btnContinue) {
            try {
                startOfflineGameFromSavedState(st, payload);
            } catch (Exception ex) {
                SysData.deleteLocalResumePayload();
            }
        } else {
            SysData.deleteLocalResumePayload();
        }
    }

    private void startOfflineGameFromSavedState(LocalGameState st, String payload) {
        if (st == null) return;

        // Reset stage state from menu (same as your startGameFromSetup)
        primaryStage.setFullScreen(false);
        primaryStage.setMaximized(false);

        primaryStage.setMinWidth(0);
        primaryStage.setMinHeight(0);
        primaryStage.setMaxWidth(Double.MAX_VALUE);
        primaryStage.setMaxHeight(Double.MAX_VALUE);
        primaryStage.setResizable(true);

        GameController.resetInstance();

        GameController controller = GameController.getInstance(
                st.difficulty,
                safeName(st.p1Name, "Player 1"),
                safeName(st.p2Name, "Player 2"),
                primaryStage,
                st.seed
        );

        Scene gameScene = new Scene(controller.gameView);
        gameScene.setFill(Color.BLACK);
        gameScene.getStylesheets().add(
                Main.class.getResource("/css/game.css").toExternalForm()
        );

        primaryStage.setScene(gameScene);

        // Apply saved state AFTER UI exists
        controller.applyLocalResumePayload(payload);

        Platform.runLater(() -> {
            primaryStage.setMaximized(true);
            primaryStage.centerOnScreen();
        });
    }

    private String safeName(String name, String def) {
        if (name == null) return def;
        String t = name.trim();
        return t.isEmpty() ? def : t;
    }

}
