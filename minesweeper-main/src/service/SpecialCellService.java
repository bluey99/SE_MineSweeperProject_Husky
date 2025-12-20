package service;

import java.util.EnumSet;
import java.util.Random;

public class SpecialCellService {

    private final Random rng;

    public SpecialCellService(Random rng) {
        this.rng = rng;
    }

    // -------------------- SURPRISE --------------------

    public SpecialCellResult processSurprise(String gameDifficulty, int currentScore, int currentLives) {

        int cost = switch (gameDifficulty) {
            case "Easy" -> 5;
            case "Medium" -> 8;
            default -> 12; // Hard
        };

        if (currentScore < cost) {
            return SpecialCellResult.blocked(
                    "Insufficient Score",
                    "You need " + cost + " points to activate this Surprise Cell.\n" +
                    "Current score: " + currentScore,
                    currentScore, currentLives
            );
        }

        int score = currentScore - cost;
        int lives = currentLives;

        boolean isGood = rng.nextBoolean();
        int bonusPoints = switch (gameDifficulty) {
            case "Easy" -> 8;
            case "Medium" -> 12;
            default -> 16; // Hard
        };

        String title;
        String msg;

        if (isGood) {
            lives += 1;
            score += bonusPoints;

            // cap lives at 10 and convert extras to points
            int converted = convertExtraLivesToPointsIfNeeded(gameDifficulty, Ref.of(lives), Ref.of(score));
            lives = Ref.LAST_LIVES;
            score = Ref.LAST_SCORE;

            if (converted > 0) {
                title = "Good Surprise! ✓";
                msg =
                    "✓ +1 life (max reached, converted +" + converted + " points)\n" +
                    "✓ +" + bonusPoints + " points\n\n" +
                    "Score: " + score + " | Lives: " + lives;
            } else {
                title = "Good Surprise! ✓";
                msg =
                    "✓ +1 life\n" +
                    "✓ +" + bonusPoints + " points\n\n" +
                    "Score: " + score + " | Lives: " + lives;
            }

        } else {
            lives -= 1;
            score -= bonusPoints;

            title = "Bad Surprise! ✗";
            msg =
                "✗ -1 life\n" +
                "✗ -" + bonusPoints + " points\n\n" +
                "Score: " + score + " | Lives: " + lives;
        }

        return SpecialCellResult.ok(title, msg, score, lives, EnumSet.noneOf(SpecialAction.class));
    }

    // -------------------- QUESTION --------------------

    public SpecialCellResult processQuestion(String gameDifficulty,
                                             String questionDifficultyLabel,
                                             boolean correct,
                                             int currentScore,
                                             int currentLives) {

        int points = 0;
        int livesDelta = 0;

        boolean grantMineGift = false;
        boolean revealArea3x3 = false;

        if ("Easy".equals(gameDifficulty)) {
            switch (questionDifficultyLabel) {
                case "Easy" -> {
                    if (correct) { points = 3; livesDelta = 1; }
                    else { if (rng.nextBoolean()) points = -3; }
                }
                case "Intermediate" -> {
                    if (correct) { points = 6; grantMineGift = true; }
                    else { if (rng.nextBoolean()) points = -6; }
                }
                case "Hard" -> {
                    if (correct) { points = 10; revealArea3x3 = true; }
                    else { points = -10; }
                }
                case "Expert" -> {
                    if (correct) { points = 15; livesDelta = 2; }
                    else { points = -15; livesDelta = -1; }
                }
            }

        } else if ("Medium".equals(gameDifficulty)) {

            switch (questionDifficultyLabel) {
                case "Easy" -> {
                    if (correct) { points = 8; livesDelta = 1; }
                    else { points = -8; }
                }
                case "Intermediate" -> {
                    if (correct) { points = 10; livesDelta = 1; }
                    else { if (rng.nextBoolean()) { points = -10; livesDelta = -1; } }
                }
                case "Hard" -> {
                    if (correct) { points = 15; livesDelta = 1; }
                    else { points = -15; livesDelta = (rng.nextBoolean() ? -1 : -2); }
                }
                case "Expert" -> {
                    if (correct) { points = 20; livesDelta = 2; }
                    else { points = -20; livesDelta = (rng.nextBoolean() ? -1 : -2); }
                }
            }

        } else { // Hard game difficulty

            switch (questionDifficultyLabel) {
                case "Easy" -> {
                    if (correct) { points = 10; livesDelta = 1; }
                    else { points = -10; livesDelta = -1; }
                }
                case "Intermediate" -> {
                    if (correct) { points = 15; livesDelta = (rng.nextBoolean() ? 1 : 2); }
                    else { points = -15; livesDelta = (rng.nextBoolean() ? -1 : -2); }
                }
                case "Hard" -> {
                    if (correct) { points = 20; livesDelta = 2; }
                    else { points = -20; livesDelta = -2; }
                }
                case "Expert" -> {
                    if (correct) { points = 40; livesDelta = 3; }
                    else { points = -40; livesDelta = -3; }
                }
            }
        }

        int score = currentScore + points;
        int lives = currentLives + livesDelta;

        // cap lives at 10 and convert extras to points
        convertExtraLivesToPointsIfNeeded(gameDifficulty, Ref.of(lives), Ref.of(score));
        lives = Ref.LAST_LIVES;
        score = Ref.LAST_SCORE;

        // build base message (controller can append extra info based on whether action succeeded)
        String msg;
        if (correct) {
            msg = "Correct ✓\n+" + Math.abs(points) + " points";
            if (livesDelta > 0) msg += ", +" + livesDelta + " lives";
        } else {
            if (points == 0 && livesDelta == 0) msg = "Wrong ✗\nNo penalty this time.";
            else {
                msg = "Wrong ✗\n" + points + " points";
                if (livesDelta != 0) msg += ", " + livesDelta + " lives";
            }
        }

        msg += "\n\nScore: " + score + " | Lives: " + lives;

        EnumSet<SpecialAction> actions = EnumSet.noneOf(SpecialAction.class);
        if (correct && grantMineGift) actions.add(SpecialAction.MINE_GIFT);
        if (correct && revealArea3x3) actions.add(SpecialAction.REVEAL_AREA_3X3);

        return SpecialCellResult.ok("Question Result", msg, score, lives, actions);
    }

    // -------------------- helpers --------------------

    /**
     * If lives > 10 => cap to 10 and convert extra lives to points.
     * Returns converted points (0 if none).
     */
    private int convertExtraLivesToPointsIfNeeded(String gameDifficulty, Ref livesRef, Ref scoreRef) {
        int lives = livesRef.value;
        int score = scoreRef.value;

        if (lives <= 10) {
            Ref.LAST_LIVES = lives;
            Ref.LAST_SCORE = score;
            return 0;
        }

        int extraLives = lives - 10;
        lives = 10;

        int perLife = switch (gameDifficulty) {
            case "Easy" -> 5;
            case "Medium" -> 8;
            default -> 12;
        };

        int converted = extraLives * perLife;
        score += converted;

        Ref.LAST_LIVES = lives;
        Ref.LAST_SCORE = score;
        return converted;
    }

    /**
     * Tiny mutable int wrapper to avoid bringing in more dependencies.
     * We store latest values in static fields for simplicity (keeps your project small).
     */
    private static class Ref {
        int value;
        private Ref(int v) { value = v; }
        static Ref of(int v) { return new Ref(v); }

        static int LAST_LIVES;
        static int LAST_SCORE;
    }
}
