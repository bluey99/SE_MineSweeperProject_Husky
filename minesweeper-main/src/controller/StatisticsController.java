package controller;

import javafx.scene.Scene;
import javafx.stage.Stage;
import model.GameHistoryEntry;
import model.SysData;
import view.StatisticsView;

import java.util.List;
import java.util.Locale;

public class StatisticsController {

    private final Stage primaryStage;
    private final StatisticsView view;

    public StatisticsController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.view = new StatisticsView();

        view.backBtn.setOnAction(e -> Main.showMainMenu(primaryStage));
        loadAndRenderStats();
    }

    private void loadAndRenderStats() {
        List<GameHistoryEntry> history = SysData.loadHistory();

        if (history.isEmpty()) {
            view.setEmptyState(true);
            return;
        }
        view.setEmptyState(false);

        int gamesPlayed = history.size();
        int winsTotal = 0;

        DiffStats easy = new DiffStats();
        DiffStats medium = new DiffStats();
        DiffStats hard = new DiffStats();

        for (GameHistoryEntry e : history) {
            boolean isWin = safe(e.getResult()).equalsIgnoreCase("WIN");
            if (isWin) winsTotal++;

            String diff = normalizeDifficulty(safe(e.getDifficulty()));
            DiffStats bucket = switch (diff) {
                case "Easy" -> easy;
                case "Medium" -> medium;
                case "Hard" -> hard;
                default -> null;
            };

            if (bucket == null) continue;

            if (isWin) {
                bucket.wins++;

                int t = e.getGameLengthSeconds();
                if (t > 0) bucket.bestTimeSec = Math.min(bucket.bestTimeSec, t);

                int s = e.getFinalScore();
                bucket.bestScore = Math.max(bucket.bestScore, s);
            }
        }

        double winRatio = (gamesPlayed == 0) ? 0.0 : (winsTotal * 100.0 / gamesPlayed);

        view.setOverall(
                String.valueOf(gamesPlayed),
                String.format("%.1f%%", winRatio)
        );

        view.setDifficultyStats(
                easy.wins, formatTimeOrDash(easy.bestTimeSec), scoreOrDash(easy.bestScore),
                medium.wins, formatTimeOrDash(medium.bestTimeSec), scoreOrDash(medium.bestScore),
                hard.wins, formatTimeOrDash(hard.bestTimeSec), scoreOrDash(hard.bestScore)
        );
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalizeDifficulty(String d) {
        if (d.isEmpty()) return "";
        String up = d.toUpperCase(Locale.ROOT);
        if (up.contains("EASY")) return "Easy";
        if (up.contains("MED")) return "Medium";
        if (up.contains("HARD")) return "Hard";
        return "";
    }

    private String formatTimeOrDash(int seconds) {
        if (seconds == Integer.MAX_VALUE || seconds <= 0) return "--:--";
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    private String scoreOrDash(int score) {
        return (score == Integer.MIN_VALUE) ? "-" : String.valueOf(score);
    }

    public Scene createScene(double width, double height) {
        return new Scene(view, width, height);
    }

    private static class DiffStats {
        int wins = 0;
        int bestTimeSec = Integer.MAX_VALUE;   // only wins
        int bestScore = Integer.MIN_VALUE;     // only wins
    }
}
