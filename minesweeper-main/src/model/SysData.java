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

    // Append a game history entry to the CSV file
    public static void saveGame(GameHistoryEntry entry) {
        File file = new File(HISTORY_FILE);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

            // Write header if file is new or empty
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

    // Load all game history entries from storage
    public static List<GameHistoryEntry> loadHistory() {
        List<GameHistoryEntry> list = new ArrayList<>();
        File file = new File(HISTORY_FILE);

        // No history available
        if (!file.exists() || file.length() == 0) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            // Skip header
            String line = reader.readLine();

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

    // Determine the current validity and state of the questions CSV file
    public static QuestionsFileStatus getQuestionsFileStatus() {
        File file = new File(QUESTIONS_FILE);

        // File does not exist
        if (!file.exists()) {
            return QuestionsFileStatus.NOT_EXISTS;
        }

        // File exists but is empty
        if (file.length() == 0) {
            return QuestionsFileStatus.EMPTY;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            // Validate header
            String header = br.readLine();
            if (header == null || !header.equals(QUESTIONS_HEADER)) {
                return QuestionsFileStatus.MALFORMED;
            }

            String line;
            boolean hasData = false;

            // Validate all data rows
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                hasData = true;

                String[] cols = line.split(",", -1);

                // Must contain exactly 8 columns
                if (cols.length != 8) {
                    return QuestionsFileStatus.MALFORMED;
                }

                // Validate ID
                String idStr = cols[0].trim();
                if (idStr.isEmpty()) {
                    return QuestionsFileStatus.MALFORMED;
                }
                Integer.parseInt(idStr);

                // Validate question text
                if (cols[1].trim().isEmpty()) {
                    return QuestionsFileStatus.MALFORMED;
                }

                // Validate difficulty
                if (QuestionDifficulty.fromString(cols[2].trim()) == null) {
                    return QuestionsFileStatus.MALFORMED;
                }

                // Validate answer options A–D
                for (int i = 3; i <= 6; i++) {
                    if (cols[i].trim().isEmpty()) {
                        return QuestionsFileStatus.MALFORMED;
                    }
                }

                // Validate correct answer
                if (letterToIndex(cols[7].trim()) == -1) {
                    return QuestionsFileStatus.MALFORMED;
                }
            }

            return hasData
                    ? QuestionsFileStatus.HAS_DATA
                    : QuestionsFileStatus.EMPTY;

        } catch (Exception e) {
            return QuestionsFileStatus.MALFORMED;
        }
    }

    // Load all valid questions from the CSV file
    public static List<Question> loadQuestions() {
        List<Question> list = new ArrayList<>();
        File file = new File(QUESTIONS_FILE);

        // No questions available
        if (!file.exists() || file.length() == 0) {
            return list;
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
                    continue; // skip malformed rows
                }

                // Remove BOM if present
                cols[0] = cols[0].replace("\uFEFF", "");

                int id = Integer.parseInt(cols[0].trim());
                String text = cols[1];
                String difficultyStr = cols[2];

                String[] options = {
                        cols[3], cols[4], cols[5], cols[6]
                };

                int correctIndex = letterToIndex(cols[7].trim());
                QuestionDifficulty diff =
                        QuestionDifficulty.fromString(difficultyStr);

                Question q = new Question(id, text, options, correctIndex, diff);
                list.add(q);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Add a new question and persist it
    public static boolean addQuestion(Question q) {
        List<Question> all = loadQuestions();

        // Assign temporary ID
        q.setId(all.size() + 1);
        all.add(q);

        // Ensure sequential IDs
        reassignQuestionIds(all);

        return saveQuestions(all);
    }

    // Update an existing question by ID
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

    // Delete a question by ID
    public static boolean deleteQuestion(Question toDelete) {
        List<Question> all = loadQuestions();

        boolean removed =
                all.removeIf(q -> q.getId() == toDelete.getId());

        if (!removed) {
            return false;
        }

        reassignQuestionIds(all);
        return saveQuestions(all);
    }

    // Write the full questions list back to the CSV file
    private static boolean saveQuestions(List<Question> questions) {
        File file = new File(QUESTIONS_FILE);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            writer.write(QUESTIONS_HEADER);
            writer.newLine();

            for (Question q : questions) {
                StringBuilder sb = new StringBuilder();

                sb.append(q.getId()).append(",");
                sb.append(escape(q.getText())).append(",");
                sb.append(q.getDifficulty()).append(",");

                for (String option : q.getOptions()) {
                    sb.append(escape(option)).append(",");
                }

                sb.append(indexToLetter(q.getCorrectIndex()));

                writer.write(sb.toString());
                writer.newLine();
            }

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    // Recreate a clean questions file with header only
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

    // Convert answer letter to index
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

    // Convert index to answer letter
    private static String indexToLetter(int idx) {
        switch (idx) {
            case 0: return "A";
            case 1: return "B";
            case 2: return "C";
            case 3: return "D";
            default: return "";
        }
    }

    // Basic escaping to avoid line breaks in CSV
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\n", " ").replace("\r", " ");
    }

    // Reassign sequential IDs starting from 1
    private static void reassignQuestionIds(List<Question> questions) {
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setId(i + 1);
        }
    }
}
