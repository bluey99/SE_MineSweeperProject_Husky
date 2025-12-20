package service;

import java.util.EnumSet;

public class SpecialCellResult {

    public final boolean allowed;          // e.g., Surprise not allowed if insufficient score
    public final String title;             // dialog title
    public final String message;           // dialog message (base)
    public final int newScore;
    public final int newLives;

    public final boolean gameOver;         // true if lives <= 0
    public final EnumSet<SpecialAction> actions;

    private SpecialCellResult(boolean allowed,
                              String title,
                              String message,
                              int newScore,
                              int newLives,
                              boolean gameOver,
                              EnumSet<SpecialAction> actions) {
        this.allowed = allowed;
        this.title = title;
        this.message = message;
        this.newScore = newScore;
        this.newLives = newLives;
        this.gameOver = gameOver;
        this.actions = actions;
    }

    public static SpecialCellResult blocked(String title, String message, int score, int lives) {
        return new SpecialCellResult(false, title, message, score, lives, lives <= 0,
                EnumSet.noneOf(SpecialAction.class));
    }

    public static SpecialCellResult ok(String title, String message,
                                       int newScore, int newLives,
                                       EnumSet<SpecialAction> actions) {
        return new SpecialCellResult(true, title, message, newScore, newLives, newLives <= 0, actions);
    }
}
