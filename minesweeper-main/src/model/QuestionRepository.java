package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionRepository {

	private static final String DB_URL = "jdbc:ucanaccess://questions.accdb";

    static {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("UCanAccess driver not found. Check your JARs.");
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------
    // Load all questions
    // -----------------------------------------------------------------
    public static List<Question> loadQuestions() {
        List<Question> list = new ArrayList<>();

        String sql = "SELECT id, question, option1, option2, option3, option4, " +
                     "correctIndex, difficulty FROM Questions1 ORDER BY id";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("question");
                String[] options = new String[4];
                options[0] = rs.getString("option1");
                options[1] = rs.getString("option2");
                options[2] = rs.getString("option3");
                options[3] = rs.getString("option4");
                int correctIndex = rs.getInt("correctIndex");
                QuestionDifficulty diff =
                        QuestionDifficulty.fromString(rs.getString("difficulty"));

                list.add(new Question(id, text, options, correctIndex, diff));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // -----------------------------------------------------------------
    // Add new question (auto ID)
    // -----------------------------------------------------------------
    public static void addQuestion(Question q) {
        String sql =
            "INSERT INTO Questions1 " +
            "(question, option1, option2, option3, option4, correctIndex, difficulty) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, q.getText());
            String[] opts = q.getOptions();
            ps.setString(2, safeOpt(opts, 0));
            ps.setString(3, safeOpt(opts, 1));
            ps.setString(4, safeOpt(opts, 2));
            ps.setString(5, safeOpt(opts, 3));
            ps.setInt(6, q.getCorrectIndex());
            ps.setString(7, q.getDifficulty().toString());
            ps.executeUpdate();

            // get generated id (works when 'id' is AutoNumber)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    q.setId(generatedId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------
    // Update existing question (by id)
    // -----------------------------------------------------------------
    public static void updateQuestion(Question q) {
        String sql =
            "UPDATE Questions1 SET " +
            "question = ?, option1 = ?, option2 = ?, option3 = ?, option4 = ?, " +
            "correctIndex = ?, difficulty = ? " +
            "WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, q.getText());
            String[] opts = q.getOptions();
            ps.setString(2, safeOpt(opts, 0));
            ps.setString(3, safeOpt(opts, 1));
            ps.setString(4, safeOpt(opts, 2));
            ps.setString(5, safeOpt(opts, 3));
            ps.setInt(6, q.getCorrectIndex());
            ps.setString(7, q.getDifficulty().toString());
            ps.setInt(8, q.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------
    // Delete question (by id)
    // -----------------------------------------------------------------
    public static void deleteQuestion(Question q) {
        String sql = "DELETE FROM Questions1 WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, q.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // helper to avoid nulls
    private static String safeOpt(String[] arr, int idx) {
        if (arr == null || idx >= arr.length || arr[idx] == null) return "";
        return arr[idx];
    }
}
