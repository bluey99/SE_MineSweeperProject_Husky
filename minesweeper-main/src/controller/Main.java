package controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.QuestionsFileStatus;
import model.SysData;
import view.Menu;
import view.SetupView;
import view.HistoryView;

public class Main extends Application {

	// Used so other screens can change the scene
	private static Stage primaryStage;

	// Hold the running Main instance so we can pass `this` to SetupView
	private static Main instance = null;

	// Preferred menu size (will be clamped to screen)
	private static final double MENU_WIDTH = 900;
	private static final double MENU_HEIGHT = 650;

	@Override
	public void start(Stage stage) {
		instance = this;
		primaryStage = stage;
		primaryStage.setTitle("Cooperative Minesweeper");

		// Show the main menu
		showMainMenu(primaryStage);

		primaryStage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0);
		});
	}

	// --- STATIC HELPERS ------------------------------------------------------

	/** Static wrapper: other classes call this to return to the main menu. */
	public static void showMainMenu(Stage stage) {
		if (instance != null) {
			instance.showMainMenuInstance(stage);
		}
	}

	/** Static getter so views can access the primary stage (e.g. SetupView). */
	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	// --- INSTANCE IMPLEMENTATION ---------------------------------------------

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
		stage.sizeToScene(); // fit window to menu

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
			// pass the current Main instance as callback
			SetupView setup = new SetupView(this);
			Scene setupScene = new Scene(setup, width, height);
			stage.setScene(setupScene);
			stage.sizeToScene();

			// keep the Setup window at menu size
			stage.setMinWidth(width);
			stage.setMaxWidth(width);
			stage.setMinHeight(height);
			stage.setMaxHeight(height);
			stage.centerOnScreen();
		});


		// History button -> open HistoryView
		menu.historyBtn.setOnAction(e -> {
		    HistoryController hc = new HistoryController(stage);
		    Scene historyScene = hc.createScene(width, height);
		    stage.setScene(historyScene);
		    stage.sizeToScene();
		
		    stage.setMinWidth(width);
		    stage.setMaxWidth(width);
		    stage.setMinHeight(height);
		    stage.setMaxHeight(height);
		    stage.centerOnScreen();
		});

		// Question Management
		menu.questionManagementBtn.setOnAction(e -> {

			QuestionsFileStatus status = SysData.getQuestionsFileStatus();

			if (status == QuestionsFileStatus.MALFORMED) {
				showQuestionsFileError(stage, width, height);
				return; // do NOT navigate
			}

			QuestionManagementController qm = new QuestionManagementController(stage);

			Scene qmScene = new Scene(qm.view, width, height);
			stage.setScene(qmScene);
			stage.sizeToScene();

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

		// --- GAME WINDOW SIZE ---
		// use full screen height, but only 92% of the width
		double gameW = screenW * 0.92;
		double gameH = screenH; // full height so bottom buttons are visible

		// center horizontally, stick to top vertically
		double x = screenX + (screenW - gameW) / 2;
		double y = screenY;

		Scene gameScene = new Scene(controller.gameView, gameW, gameH);
		primaryStage.setScene(gameScene);

		primaryStage.setX(x);
		primaryStage.setY(y);
		primaryStage.setWidth(gameW);
		primaryStage.setHeight(gameH);

		// Lock size so layout stays stable
		primaryStage.setMinWidth(gameW);
		primaryStage.setMaxWidth(gameW);
		primaryStage.setMinHeight(gameH);
		primaryStage.setMaxHeight(gameH);

		primaryStage.setResizable(false);
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

	public static void main(String[] args) {
		launch(args);
	}
}
