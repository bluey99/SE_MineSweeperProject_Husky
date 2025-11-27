
package controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.Menu;
import view.SetupView;

public class Main extends Application {

    private Stage primaryStage;   // We need to store the main window

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("Cooperative Minesweeper");

        // Show MAIN MENU first
        Menu menu = new Menu();
        Scene menuScene = new Scene(menu, 600, 500);

        primaryStage.setScene(menuScene);
        primaryStage.show();

        // When START GAME is clicked -> open SetupView
        menu.startBtn.setOnAction(e -> {
            SetupView setup = new SetupView(this);   // Pass Main to SetupView
            Scene setupScene = new Scene(setup, 600, 750);
            primaryStage.setScene(setupScene);
            primaryStage.centerOnScreen();
        });

        // History button (future)
        menu.historyBtn.setOnAction(e -> {
            System.out.println("History clicked");
        });

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
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
