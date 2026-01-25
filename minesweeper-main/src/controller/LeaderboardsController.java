package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.GameHistoryEntry;
import model.LeaderboardEntry;
import model.SysData;
import view.LeaderboardsView;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardsController {

    private final Stage primaryStage;
    private final LeaderboardsView view;

    public LeaderboardsController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.view = new LeaderboardsView();

        view.backBtn.setOnAction(e -> Main.showMainMenu(primaryStage));

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        List<GameHistoryEntry> history = SysData.loadHistory();

        if (history.isEmpty()) {
            view.setEmptyState(true);
            return;
        }
        view.setEmptyState(false);

        Map<String, DuoAgg> duoMap = new HashMap<>();

        for (GameHistoryEntry e : history) {
            String p1Raw = safe(e.getPlayer1Name());
            String p2Raw = safe(e.getPlayer2Name());
            if (p1Raw.isEmpty() || p2Raw.isEmpty()) continue;

            String p1 = normalizeName(p1Raw);
            String p2 = normalizeName(p2Raw);
            if (p1.isEmpty() || p2.isEmpty()) continue;

            // Canonical duo key (order independent)
            String a = p1.compareTo(p2) <= 0 ? p1 : p2;
            String b = p1.compareTo(p2) <= 0 ? p2 : p1;
            String key = a + " | " + b;

            DuoAgg agg = duoMap.computeIfAbsent(key, k -> new DuoAgg());

            agg.games++;

            boolean isWin = safe(e.getResult()).equalsIgnoreCase("WIN");
            if (isWin) agg.wins++;

            // Store nice display names (title-cased)
            agg.displayA = titleCase(a);
            agg.displayB = titleCase(b);
        }

        if (duoMap.isEmpty()) {
            view.setEmptyState(true);
            return;
        }

        List<Map.Entry<String, DuoAgg>> sorted = duoMap.entrySet().stream()
                .sorted((x, y) -> {
                    DuoAgg ax = x.getValue();
                    DuoAgg ay = y.getValue();
                    int c = Integer.compare(ay.wins, ax.wins);      // wins desc
                    if (c != 0) return c;
                    c = Integer.compare(ay.games, ax.games);        // games desc
                    if (c != 0) return c;
                    return x.getKey().compareTo(y.getKey());        // stable
                })
                .collect(Collectors.toList());

        ObservableList<LeaderboardEntry> rows = FXCollections.observableArrayList();

        int rank = 1;
        for (Map.Entry<String, DuoAgg> entry : sorted) {
            DuoAgg a = entry.getValue();

            String duoName = a.displayA + " & " + a.displayB;
            String rate = (a.games == 0) ? "0.0%" : String.format("%.1f%%", (a.wins * 100.0 / a.games));

            rows.add(new LeaderboardEntry(rank, duoName, a.wins, a.games, rate));
            rank++;

            if (rank > 10) break; // top 10 fits screen nicely
        }

        view.table.setItems(rows);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalizeName(String name) {
        // trim + collapse spaces + lower-case
        String t = name.trim().replaceAll("\\s+", " ");
        return t.toLowerCase(Locale.ROOT);
    }

    private String titleCase(String normalizedLower) {
        if (normalizedLower == null || normalizedLower.isEmpty()) return "";
        String[] parts = normalizedLower.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1));
            if (i < parts.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    public Scene createScene(double width, double height) {
        return new Scene(view, width, height);
    }

    private static class DuoAgg {
        int wins = 0;
        int games = 0;
        String displayA = "";
        String displayB = "";
    }
}
