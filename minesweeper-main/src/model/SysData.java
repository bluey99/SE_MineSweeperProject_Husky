package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Central data access class.
 * Handles:
 *  - Game history (game_history.csv)
 *  - Trivia questions (QuestionsCSV.csv)
 *
 * Controllers MUST NOT access files directly.
 */
public class SysData {

    // ===================== HISTORY CONFIG =====================
    private static final String HISTORY_FILE = "game_history.csv";
    private static final String HISTORY_HEADER =
            "dateTime,difficulty,player1Name,player2Name,result,finalScore,gameLengthSeconds";

    // ===================== QUESTIONS CONFIG =====================
    // CSV columns: ID,Question,Difficulty,A,B,C,D,Correct Answer
    private static final String QUESTIONS_FILE = "QuestionsCSV.csv";
    private static final String QUESTIONS_HEADER =
            "ID,Question,Difficulty,A,B,C,D,Correct Answer";

    // ============================================================
    //                      GAME HISTORY API
    // ============================================================
    public static void saveGame(GameHistoryEntry entry) {
        File file = new File(HISTORY_FILE);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

            // if file is new or empty → write header first
            if (!file.exists() || file.length() == 0) {
                writer.write(HISTORY_HEADER);
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

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 7) continue;

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

    // ============================================================
    //                      QUESTIONS API
    // ============================================================

    /**
     * Load all questions from the QuestionsCSV.csv file.
     */
    public static List<Question> loadQuestions() {
        List<Question> list = new ArrayList<>();
        File file = new File(QUESTIONS_FILE);

        if (!file.exists() || file.length() == 0) {
            return list; // no questions yet
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {

                // Skip header
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(",", -1);
                if (cols.length < 8) {
                    // malformed line, skip
                    continue;
                }

                // Remove BOM from first column if present
                cols[0] = cols[0].replace("\uFEFF", "");

                int id = Integer.parseInt(cols[0].trim());
                String text = cols[1];
                String difficultyStr = cols[2];

                String[] options = new String[4];
                options[0] = cols[3]; // A
                options[1] = cols[4]; // B
                options[2] = cols[5]; // C
                options[3] = cols[6]; // D

                String correctLetter = cols[7].trim();
                int correctIndex = letterToIndex(correctLetter);

                QuestionDifficulty diff = QuestionDifficulty.fromString(difficultyStr);

                Question q = new Question(id, text, options, correctIndex, diff);
                list.add(q);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Add a new question to storage.
     * Simple implementation:
     *  - Load all questions
     *  - Add new one
     *  - Rewrite file
     */
    public static void addQuestion(Question q) {
        List<Question> all = loadQuestions();
        all.add(q);
        saveQuestions(all);
    }

    /**
     * Update an existing question in storage.
     * This assumes equality by ID (adjust if your model is different).
     */
    public static void updateQuestion(Question updated) {
        List<Question> all = loadQuestions();

        for (int i = 0; i < all.size(); i++) {
            Question existing = all.get(i);
            if (existing.getId() == updated.getId()) {  // adjust getter name if needed
                all.set(i, updated);
                break;
            }
        }

        saveQuestions(all);
    }

    /**
     * Delete a question from storage.
     */
    public static void deleteQuestion(Question toDelete) {
        List<Question> all = loadQuestions();
        all.removeIf(q -> q.getId() == toDelete.getId()); // adjust getter if needed
        saveQuestions(all);
    }

    /**
     * Helper: write full list of questions back to CSV.
     */
    private static void saveQuestions(List<Question> questions) {
        File file = new File(QUESTIONS_FILE);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            // header
            writer.write(QUESTIONS_HEADER);
            writer.newLine();

            for (Question q : questions) {
                // Assuming Question has appropriate getters; adjust names if needed.
                int id = q.getId();
                String text = q.getText();
                QuestionDifficulty diff = q.getDifficulty();
                String[] options = q.getOptions(); // length 4: A,B,C,D
                int correctIndex = q.getCorrectIndex();

                String correctLetter = indexToLetter(correctIndex);

                StringBuilder sb = new StringBuilder();
                sb.append(id).append(",");
                sb.append(escape(text)).append(",");
                sb.append(diff.toString()).append(",");
                sb.append(escape(options[0])).append(",");
                sb.append(escape(options[1])).append(",");
                sb.append(escape(options[2])).append(",");
                sb.append(escape(options[3])).append(",");
                sb.append(correctLetter);

                writer.write(sb.toString());
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ====== Helpers ======

    private static int letterToIndex(String letter) {
        if (letter == null) return -1;
        switch (letter.toUpperCase()) {
            case "A": return 0;
            case "B": return 1;
            case "C": return 2;
            case "D": return 3;
            default:  return -1;
        }
    }

    private static String indexToLetter(int idx) {
        switch (idx) {
            case 0: return "A";
            case 1: return "B";
            case 2: return "C";
            case 3: return "D";
            default: return "";
        }
    }

    // Very simple escaping – if you want to support commas inside text, improve this.
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\n", " ").replace("\r", " ");
    }
}
