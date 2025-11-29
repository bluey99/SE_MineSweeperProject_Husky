package model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class QuestionRepository {

    // Path to your CSV file (relative to project root).
    // If you put it in another folder, change this path.
    private static final String CSV_PATH = "QuestionsCSV.csv";

    // -----------------------------------------------------------------
    // Load all questions from CSV
    // CSV columns:
    // ID,Question,Difficulty,A,B,C,D,Correct Answer
    // Correct Answer is one of: A / B / C / D
    // -----------------------------------------------------------------
    public static List<Question> loadQuestions() {
        List<Question> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(CSV_PATH),
                        StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {

                // Skip header
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                // Split row into columns
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

                list.add(new Question(id, text, options, correctIndex, diff));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Map A/B/C/D -> 0/1/2/3
    private static int letterToIndex(String letter) {
        if (letter == null) return -1;

        switch (letter.toUpperCase()) {
            case "A":
                return 0;
            case "B":
                return 1;
            case "C":
                return 2;
            case "D":
                return 3;
            default:
                return -1;
        }
    }

    // -----------------------------------------------------------------
    // The following methods are NOT implemented for CSV.
    // If your game/editor never calls them, they will never be used.
    // If you want, we can later implement "edit CSV" logic as well.
    // -----------------------------------------------------------------
    public static void addQuestion(Question q) {
        throw new UnsupportedOperationException(
                "addQuestion is not supported with CSV storage");
    }

    public static void updateQuestion(Question q) {
        throw new UnsupportedOperationException(
                "updateQuestion is not supported with CSV storage");
    }

    public static void deleteQuestion(Question q) {
        throw new UnsupportedOperationException(
                "deleteQuestion is not supported with CSV storage");
    }

    // helper from old code kept for compatibility (not used now)
    @SuppressWarnings("unused")
    private static String safeOpt(String[] arr, int idx) {
        if (arr == null || idx >= arr.length || arr[idx] == null) return "";
        return arr[idx];
    }
}
