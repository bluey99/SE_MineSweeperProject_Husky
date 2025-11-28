package controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.Menu;
import view.SetupView;

public class Main extends Application {

    private static Stage primaryStage;   // make static so controller can call showMainMenu

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
        Scene menuScene = new Scene(menu, 600, 500);

        stage.setScene(menuScene);
        stage.show();

        // Start game -> Setup
        menu.startBtn.setOnAction(e -> {
            SetupView setup = new SetupView(new Main()); // we only need Main for callback
            Scene setupScene = new Scene(setup, 600, 750);
            stage.setScene(setupScene);
            stage.centerOnScreen();
        });

        // History button (still stub)
        menu.historyBtn.setOnAction(e -> {
            System.out.println("History clicked");
        });

        // Question Management
        menu.questionManagementBtn.setOnAction(e -> {
            QuestionManagementController qm = new QuestionManagementController(stage);
            stage.setScene(qm.createScene());
            stage.centerOnScreen();
        });
    }

    /**
     * Called from SetupView after validation is done.
     */
    public void startGameFromSetup(String p1, String p2, String difficulty) {
        GameController controller = new GameController(difficulty, p1, p2);

        int width = 1000;
        int height = 700;

        switch (difficulty) {
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
