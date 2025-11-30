package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SysData {

    private static final String HISTORY_FILE = "game_history.csv";
    private static final String HEADER =
            "dateTime,difficulty,player1Name,player2Name,result,finalScore,gameLengthSeconds";

    public static void saveGame(GameHistoryEntry entry) {
        File file = new File(HISTORY_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {

            if (!file.exists() || file.length() == 0) {
                writer.write(HEADER);
                writer.newLine();
            }

            writer.write(entry.toCsvRow());
            writer.newLine();

        } catch (IOException e) {
            System.err.println("Failed to write to history file: " + e.getMessage());
        }
    }

    public static List<GameHistoryEntry> loadHistory() {
        List<GameHistoryEntry> list = new ArrayList<>();
        File file = new File(HISTORY_FILE);

        if (!file.exists() || file.length() == 0) {
            return list; // no history yet
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // very simple CSV parsing (assumes no commas inside fields)
                String[] parts = line.split(",", -1);
                if (parts.length < 7) continue; // bad row, skip

                String dateTime   = parts[0];
                String difficulty = parts[1];
                String p1         = parts[2];
                String p2         = parts[3];
                String result     = parts[4];
                int finalScore    = Integer.parseInt(parts[5]);
                int gameLength    = Integer.parseInt(parts[6]);

                GameHistoryEntry entry = new GameHistoryEntry(
                        dateTime, difficulty, p1, p2, result, finalScore, gameLength
                );
                list.add(entry);
            }

        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load history: " + e.getMessage());
        }

        return list;
    }
}
