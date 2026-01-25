package view.dialogs;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class HelpDialog {

    private int currentStep = 0;

    // ===================== STEPS =====================

    private final List<HelpStep> steps = List.of(

    	    new HelpStep(
    	    		"Welcome to MineMates",
                    """
    	MineMates is a cooperative Minesweeper game for two players.

    	You and your teammate work as one team,
    	trying to clear all safe cells.

    	You do not compete.
    	You either win together — or lose together.
    	"""
    	    ),

    	    new HelpStep(
    	            "Boards & Turns",
    	            """
    	Each player has their own board.

    	Players take turns.

    	On your turn, you can interact
    	ONLY with your own board.

    	Your teammate watches and advises —
    	but cannot play for you.
    	"""
    	    ),

    	    new HelpStep(
    	            "Mouse Controls",
    	            """
    	You control the game using the mouse:

    	• LEFT-CLICK:
    	  Reveal a cell or activate a special cell

    	• RIGHT-CLICK:
    	  Place or remove a flag

    	Flags help you mark dangerous cells.
    	"""
    	    ),

    	    new HelpStep(
    	            "Revealing Cells",
    	            """
    	Left-clicking a cell reveals it.

    	If the cell is safe:
    	• A number may appear
    	• Or the cell may be empty

    	If the cell contains a mine:
    	• A shared life is lost
    	"""
    	    ),

    	    new HelpStep(
    	            "What the Numbers Mean",
    	            """
    	Numbers show how many mines
    	are touching that cell.

    	For example:
    	• 1 → one nearby mine
    	• 3 → three nearby mines

    	Nearby includes all surrounding cells —
    	up, down, sides, and diagonals.
    	"""
    	    ),

    	    new HelpStep(
    	            "Flags",
    	            """
    	Right-click to place a flag.

    	Flags are reminders —
    	they do NOT protect you.

    	You can still lose a life
    	if you reveal a flagged mine.

    	Use flags to think ahead,
    	not to take risks.
    	"""
    	    ),

    	    new HelpStep(
    	            "Shared Lives",
    	            """
    	Both players share ONE lives pool.

    	If a mine is triggered on either board,
    	the team loses a life.

    	When all lives are gone,
    	the game ends for both players.
    	"""
    	    ),

    	    new HelpStep(
    	            "Surprise Cells 🎁",
    	            """
    	Surprise cells are special tiles.

    	When left-clicked, they activate
    	a random effect with a 50/50 chance:

    	• A GOOD surprise that helps the team
    	• A BAD surprise that causes harm

    	The effect depends on game difficulty.
    	"""
    	    ),

    	    new HelpStep(
    	            "Question Cells ❓",
    	            """
    	Question cells trigger a trivia question.

    	Each question:
    	• Has 4 possible answers
    	• Only one is correct

    	Questions come in levels:
    	Easy • Medium • Hard • Expert
    	"""
    	    ),

    	    new HelpStep(
    	            "Rewards & Punishments",
    	            """
    	Answering correctly gives a reward.
    	Answering wrong causes a punishment.

    	The strength of the effect depends on:
    	• Game difficulty
    	• Question difficulty

    	Harder questions mean higher risk —
    	and greater reward.
    	"""
    	    ),

    	    new HelpStep(
    	            "Difficulty Levels",
    	            """
    	Each difficulty level changes the game.

    	Higher difficulties usually mean:
    	• Fewer shared lives
    	• Stronger punishments
    	• More valuable rewards

    	Team coordination matters more
    	as difficulty increases.
    	"""
    	    ),

    	    new HelpStep(
    	            "Winning the Game",
    	            """
    	To win, the team must reveal
    	ALL safe cells on both boards.

    	There is no solo victory.
    	There is no solo failure.

    	Think together.
    	Communicate.
    	Win as a team.
    	"""
    	    )
    	);


    // ===================== UI STATE =====================

    private StackPane contentStack;
    private Text titleText;
    private HBox indicators;

    // ===================== SHOW =====================

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("How to Play");
        stage.setWidth(620);
        stage.setHeight(460);
        stage.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(22));
        root.setBackground(new Background(new BackgroundFill(
                Color.web("#020617"), CornerRadii.EMPTY, Insets.EMPTY
        )));

        // ===== CARD =====
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        VBox.setVgrow(card, Priority.ALWAYS);

        card.setBackground(new Background(new BackgroundFill(
                Color.web("rgba(255,255,255,0.06)"),
                new CornerRadii(18),
                Insets.EMPTY
        )));
        card.setBorder(new Border(new BorderStroke(
                Color.web("rgba(56,189,248,0.45)"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(18),
                new BorderWidths(1.4)
        )));
        card.setEffect(new DropShadow(18, Color.rgb(0, 0, 0, 0.45)));

        titleText = new Text();
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleText.setFill(Color.web("#E5E7EB"));

        contentStack = new StackPane();
        VBox.setVgrow(contentStack, Priority.ALWAYS);

        indicators = new HBox(6);
        indicators.setAlignment(Pos.CENTER);

        card.getChildren().addAll(titleText, contentStack, indicators);

        // ===== CONTROLS =====
        Button backBtn = new Button("Back");
        Button nextBtn = new Button("Next");

        styleSecondary(backBtn);
        stylePrimary(nextBtn);

        HBox controls = new HBox(10, backBtn, nextBtn);
        controls.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(card, controls);

        // INITIAL
        setStepContent(steps.get(0), false);
        updateIndicators();

        backBtn.setDisable(true);

        backBtn.setOnAction(e -> {
            currentStep--;
            animateToStep(steps.get(currentStep), true);
            backBtn.setDisable(currentStep == 0);
            nextBtn.setText("Next");
        });

        nextBtn.setOnAction(e -> {
            if (currentStep < steps.size() - 1) {
                currentStep++;
                animateToStep(steps.get(currentStep), false);
                backBtn.setDisable(false);
                if (currentStep == steps.size() - 1) {
                    nextBtn.setText("Close");
                }
            } else {
                stage.close();
            }
        });

        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    // ===================== CONTENT =====================

    private void animateToStep(HelpStep step, boolean backwards) {
        Node newNode = createBody(step.body);
        newNode.setTranslateX(backwards ? -600 : 600);

        Node old = contentStack.getChildren().get(0);
        contentStack.getChildren().add(newNode);
        titleText.setText(step.title);
        updateIndicators();

        TranslateTransition out = new TranslateTransition(Duration.millis(260), old);
        out.setToX(backwards ? 600 : -600);

        TranslateTransition in = new TranslateTransition(Duration.millis(260), newNode);
        in.setToX(0);

        out.setOnFinished(e -> contentStack.getChildren().remove(old));

        out.play();
        in.play();
    }

    private void setStepContent(HelpStep step, boolean animate) {
        titleText.setText(step.title);
        contentStack.getChildren().setAll(createBody(step.body));
        updateIndicators();
    }

    private Node createBody(String text) {
        Text body = new Text(text);
        body.setFont(Font.font("Arial", 14));
        body.setFill(Color.web("#CBD5F5"));
        body.setTextAlignment(TextAlignment.LEFT);
        body.setWrappingWidth(520);

        VBox wrapper = new VBox(body);
        wrapper.setAlignment(Pos.TOP_LEFT);
        wrapper.setMinHeight(Region.USE_COMPUTED_SIZE);
        wrapper.setPrefHeight(Region.USE_COMPUTED_SIZE);
        wrapper.setMaxHeight(Double.MAX_VALUE);

        return wrapper;
    }

    // ===================== INDICATORS =====================

    private void updateIndicators() {
        indicators.getChildren().clear();
        for (int i = 0; i < steps.size(); i++) {
            Text dot = new Text(i == currentStep ? "●" : "○");
            dot.setFill(i == currentStep
                    ? Color.web("#38BDF8")
                    : Color.web("#64748B"));
            dot.setFont(Font.font(12));
            indicators.getChildren().add(dot);
        }
    }

    // ===================== STYLES =====================

    private void stylePrimary(Button btn) {
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setPrefHeight(38);
        btn.setStyle(
                "-fx-background-color: #2563EB;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 14;" +
                "-fx-padding: 0 18;" +
                "-fx-cursor: hand;"
        );
    }

    private void styleSecondary(Button btn) {
        btn.setFont(Font.font("Arial", 13));
        btn.setPrefHeight(38);
        btn.setStyle(
                "-fx-background-color: rgba(15,23,42,0.85);" +
                "-fx-text-fill: #E5E7EB;" +
                "-fx-background-radius: 14;" +
                "-fx-padding: 0 18;" +
                "-fx-cursor: hand;"
        );
    }

    // ===================== MODEL =====================

    private static class HelpStep {
        final String title;
        final String body;

        HelpStep(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }
}
