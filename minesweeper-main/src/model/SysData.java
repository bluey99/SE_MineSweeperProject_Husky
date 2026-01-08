package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Central data access class.
 *
 * Handles:
 * - Game history (game_history.csv)
 * - Trivia questions (QuestionsCSV.csv)
 *
 * Controllers and views MUST NOT access files directly.
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

    /** Append a game history entry */
    public static void saveGame(GameHistoryEntry entry) {
        File file = new File(HISTORY_FILE);

        // ✅ IMPORTANT: decide header BEFORE opening stream (opening can create the file)
        boolean writeHeader = (!file.exists() || file.length() == 0);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

            if (writeHeader) {
                writer.write(HISTORY_HEADER);
                writer.newLine();
            }

            writer.write(entry.toCsvRow());
            writer.newLine();

        } catch (IOException e) {
            System.err.println("Failed to write to history file: " + e.getMessage());
        }
    }

    /** Load all history entries */
    public static List<GameHistoryEntry> loadHistory() {
        List<GameHistoryEntry> list = new ArrayList<>();
        File file = new File(HISTORY_FILE);

        if (!file.exists() || file.length() == 0) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            reader.readLine(); // skip header
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 7) continue;

                GameHistoryEntry entry = new GameHistoryEntry(
                        parts[0],
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4],
                        Integer.parseInt(parts[5]),
                        Integer.parseInt(parts[6])
                );

                list.add(entry);
            }

        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load history: " + e.getMessage());
        }

        return list;
    }

    // ============================================================
    //                  HISTORY ADMIN OPERATIONS
    // ============================================================

    /** Remove all history entries (keep header) */
    public static int clearHistory() {
        List<GameHistoryEntry> all = loadHistory();
        rewriteHistory(new ArrayList<>());
        return all.size();
    }

    /** Keep only the last N history entries */
    public static int trimHistory(int keepN) {
        List<GameHistoryEntry> all = loadHistory();

        if (keepN >= all.size()) return 0;

        if (keepN <= 0) {
            int removed = all.size();
            rewriteHistory(new ArrayList<>());
            return removed;
        }

        int fromIndex = Math.max(0, all.size() - keepN);
        List<GameHistoryEntry> kept = new ArrayList<>(all.subList(fromIndex, all.size()));

        int removed = all.size() - kept.size();
        rewriteHistory(kept);
        return removed;
    }

    /** Safely delete a specific history entry */
    public static boolean deleteHistoryEntry(GameHistoryEntry target) {
        List<GameHistoryEntry> all = loadHistory();

        boolean removed = all.removeIf(e -> sameEntry(e, target));
        if (!removed) return false;

        rewriteHistory(all);
        return true;
    }

    /** Rewrite history CSV from scratch */
    private static void rewriteHistory(List<GameHistoryEntry> entries) {
        File file = new File(HISTORY_FILE);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            writer.write(HISTORY_HEADER);
            writer.newLine();

            for (GameHistoryEntry e : entries) {
                writer.write(e.toCsvRow());
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Failed to rewrite history: " + e.getMessage());
        }
    }

    /** Strict comparison for safe deletion */
    private static boolean sameEntry(GameHistoryEntry a, GameHistoryEntry b) {
        if (a == null || b == null) return false;

        return safeEq(a.getDateTime(), b.getDateTime())
                && safeEq(a.getDifficulty(), b.getDifficulty())
                && safeEq(a.getPlayer1Name(), b.getPlayer1Name())
                && safeEq(a.getPlayer2Name(), b.getPlayer2Name())
                && safeEq(a.getResult(), b.getResult())
                && a.getFinalScore() == b.getFinalScore()
                && a.getGameLengthSeconds() == b.getGameLengthSeconds();
    }

    private static boolean safeEq(String x, String y) {
        if (x == null && y == null) return true;
        if (x == null || y == null) return false;
        return x.equals(y);
    }

    // ============================================================
    //                      QUESTIONS API
    // ============================================================

    /** Validate questions CSV state */
    public static QuestionsFileStatus getQuestionsFileStatus() {
        File file = new File(QUESTIONS_FILE);

        if (!file.exists()) return QuestionsFileStatus.NOT_EXISTS;
        if (file.length() == 0) return QuestionsFileStatus.EMPTY;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) return QuestionsFileStatus.MALFORMED;

            // ✅ Remove BOM if present + trim
            header = header.replace("\uFEFF", "").trim();

            if (!header.equals(QUESTIONS_HEADER)) {
                return QuestionsFileStatus.MALFORMED;
            }

            String line;
            boolean hasData = false;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                hasData = true;

                // NOTE: this simple split assumes no quoted commas in fields.
                String[] cols = line.split(",", -1);
                if (cols.length != 8) return QuestionsFileStatus.MALFORMED;

                // Validate ID
                String idStr = cols[0].trim().replace("\uFEFF", "");
                if (idStr.isEmpty()) return QuestionsFileStatus.MALFORMED;
                Integer.parseInt(idStr);

                // Validate question text
                if (cols[1].trim().isEmpty()) return QuestionsFileStatus.MALFORMED;

                // Validate difficulty
                if (QuestionDifficulty.fromString(cols[2].trim()) == null)
                    return QuestionsFileStatus.MALFORMED;

                // Validate answer options A–D
                for (int i = 3; i <= 6; i++) {
                    if (cols[i].trim().isEmpty())
                        return QuestionsFileStatus.MALFORMED;
                }

                // Validate correct answer
                if (letterToIndex(cols[7].trim()) == -1)
                    return QuestionsFileStatus.MALFORMED;
            }

            return hasData ? QuestionsFileStatus.HAS_DATA : QuestionsFileStatus.EMPTY;

        } catch (Exception e) {
            return QuestionsFileStatus.MALFORMED;
        }
    }

    /** Load all questions */
    public static List<Question> loadQuestions() {
        List<Question> list = new ArrayList<>();
        File file = new File(QUESTIONS_FILE);

        if (!file.exists() || file.length() == 0) return list;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // skip header
                }

                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(",", -1);
                if (cols.length < 8) continue;

                // Remove BOM if present
                cols[0] = cols[0].replace("\uFEFF", "");

                Question q = QuestionFactory.createQuestion(
                        Integer.parseInt(cols[0].trim()),
                        cols[1],
                        new String[]{cols[3], cols[4], cols[5], cols[6]},
                        letterToIndex(cols[7].trim()),
                        QuestionDifficulty.fromString(cols[2].trim())
                );

                list.add(q);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean addQuestion(Question q) {
        List<Question> all = loadQuestions();
        q.setId(all.size() + 1);
        all.add(q);
        reassignQuestionIds(all);
        return saveQuestions(all);
    }

    public static boolean updateQuestion(Question updated) {
        List<Question> all = loadQuestions();

        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == updated.getId()) {
                all.set(i, updated);
                break;
            }
        }

        reassignQuestionIds(all);
        return saveQuestions(all);
    }

    public static boolean deleteQuestion(Question toDelete) {
        List<Question> all = loadQuestions();

        boolean removed = all.removeIf(q -> q.getId() == toDelete.getId());
        if (!removed) return false;

        reassignQuestionIds(all);
        return saveQuestions(all);
    }

    /** Write full list of questions back to CSV */
    private static boolean saveQuestions(List<Question> questions) {
        File file = new File(QUESTIONS_FILE);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            writer.write(QUESTIONS_HEADER);
            writer.newLine();

            for (Question q : questions) {
                writer.write(
                        q.getId() + "," +
                                escapeCsv(q.getText()) + "," +
                                q.getDifficulty() + "," +
                                escapeCsv(q.getOptions()[0]) + "," +
                                escapeCsv(q.getOptions()[1]) + "," +
                                escapeCsv(q.getOptions()[2]) + "," +
                                escapeCsv(q.getOptions()[3]) + "," +
                                indexToLetter(q.getCorrectIndex())
                );
                writer.newLine();
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Recreate a clean questions file with header only */
    public static boolean recreateQuestionsFile() {
        File file = new File(QUESTIONS_FILE);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            writer.write(QUESTIONS_HEADER);
            writer.newLine();
            return true;

        } catch (IOException e) {
            return false;
        }
    }

    // ============================================================
    //                      HELPERS
    // ============================================================

    private static int letterToIndex(String letter) {
        if (letter == null) return -1;
        switch (letter.toUpperCase()) {
            case "A": return 0;
            case "B": return 1;
            case "C": return 2;
            case "D": return 3;
            default: return -1;
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

    /**
     * ✅ Proper CSV escaping:
     * - replaces newlines
     * - wraps in quotes if contains comma or quote
     * - doubles internal quotes
     */
    private static String escapeCsv(String s) {
        if (s == null) return "";
        String cleaned = s.replace("\r", " ").replace("\n", " ");
        boolean mustQuote = cleaned.contains(",") || cleaned.contains("\"");
        cleaned = cleaned.replace("\"", "\"\"");
        return mustQuote ? "\"" + cleaned + "\"" : cleaned;
    }

    private static void reassignQuestionIds(List<Question> questions) {
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setId(i + 1);
        }
    }
}
