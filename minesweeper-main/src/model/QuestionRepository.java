package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class QuestionRepository {

    private static final String CSV_PATH = "QuestionsCSV.csv";

    // -------------------------------------------------------------
    // Load questions from CSV (same as before)
    // -------------------------------------------------------------
    public static List<Question> loadQuestions() {
        List<Question> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(CSV_PATH),
                        StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {

                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] cols = line.split(",", -1);
                if (cols.length < 8) continue;

                cols[0] = cols[0].replace("\uFEFF", "");

                int id = Integer.parseInt(cols[0].trim());
                String text = cols[1];
                String difficultyStr = cols[2];

                String[] options = new String[4];
                options[0] = cols[3];
                options[1] = cols[4];
                options[2] = cols[5];
                options[3] = cols[6];

                int correctIndex = letterToIndex(cols[7].trim());
                QuestionDifficulty diff = QuestionDifficulty.fromString(difficultyStr);

                list.add(new Question(id, text, options, correctIndex, diff));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

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

    private static String indexToLetter(int i) {
        switch (i) {
            case 0: return "A";
            case 1: return "B";
            case 2: return "C";
            case 3: return "D";
            default: return "";
        }
    }

    // -------------------------------------------------------------
    // 🔹 Save full list back to CSV (rewrite the whole file)
    // bayan added here - CSV writing support
    // -------------------------------------------------------------
    private static void saveAll(List<Question> list) {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(CSV_PATH),
                        StandardCharsets.UTF_8))) {

            pw.println("ID,Question,Difficulty,A,B,C,D,Correct Answer");

            for (Question q : list) {
                pw.println(
                        q.getId() + "," +
                        sanitize(q.getText()) + "," +
                        q.getDifficulty() + "," +
                        sanitize(q.getOptions()[0]) + "," +
                        sanitize(q.getOptions()[1]) + "," +
                        sanitize(q.getOptions()[2]) + "," +
                        sanitize(q.getOptions()[3]) + "," +
                        indexToLetter(q.getCorrectIndex())
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Avoid commas breaking CSV format
    private static String sanitize(String s) {
        return s == null ? "" : s.replace(",", " ");
    }

    // -------------------------------------------------------------
    // 🔹 Add Question
    // bayan added here - enable adding questions to CSV
    // -------------------------------------------------------------
    public static void addQuestion(Question q) {
        List<Question> list = loadQuestions();

        // auto-generate new ID
        int newId = list.stream()
                .mapToInt(Question::getId)
                .max()
                .orElse(0) + 1;

        q.setId(newId);

        list.add(q);
        saveAll(list);
    }

    // -------------------------------------------------------------
    // 🔹 Update Question
    // bayan added here - enable editing questions in CSV
    // -------------------------------------------------------------
    public static void updateQuestion(Question q) {
        List<Question> list = loadQuestions();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == q.getId()) {
                list.set(i, q);
                break;
            }
        }

        saveAll(list);
    }

    // -------------------------------------------------------------
    // 🔹 Delete Question
    // bayan added here - enable deleting questions in CSV
    // -------------------------------------------------------------
    public static void deleteQuestion(Question q) {
        List<Question> list = loadQuestions();
        list.removeIf(item -> item.getId() == q.getId());
        saveAll(list);
    }
}
