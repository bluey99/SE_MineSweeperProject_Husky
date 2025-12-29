package unittests;

import model.Question;
import model.QuestionDifficulty;
import model.SysData;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *  tests for SysData question persistence logic.
 *
 * Focus:
 * - Internal CSV handling
 * - ID assignment and reassignment logic
 * - File overwrite behavior after add/delete operations
 */
public class SysDataTest {

    // Path to the real CSV file used by SysData
    private static final Path CSV_PATH =
            Paths.get(System.getProperty("user.dir"), "QuestionsCSV.csv");

    // Expected CSV header (must match SysData exactly)
    private static final String HEADER =
            "ID,Question,Difficulty,A,B,C,D,Correct Answer";

    // Backup storage for restoring the original CSV after each test
    private byte[] backupBytes = null;
    private boolean hadOriginalFile = false;

    /**
     * Runs before each test.
     * Creates a clean CSV environment to ensure test isolation.
     * Backs up the original file if it exists.
     */
    @BeforeEach
    void backupAndPrepareCleanCsv() throws IOException {
        hadOriginalFile = Files.exists(CSV_PATH);

        // Backup existing CSV (if present)
        if (hadOriginalFile) {
            backupBytes = Files.readAllBytes(CSV_PATH);
        }

        // Create a fresh CSV with header only
        Files.write(
                CSV_PATH,
                (HEADER + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    /**
     * Runs after each test.
     * Restores the CSV file to its original state to avoid side effects.
     */
    @AfterEach
    void restoreOriginalCsv() throws IOException {
        if (hadOriginalFile && backupBytes != null) {
            // Restore original content exactly
            Files.write(
                    CSV_PATH,
                    backupBytes,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } else {
            // Remove test file if none existed before
            Files.deleteIfExists(CSV_PATH);
        }
    }

    /**
     * White-box test:
     * Verifies that deleting a question triggers ID reassignment
     * and results in sequential IDs with no gaps.
     */
    @Test
    void testDeleteQuestion_ReassignsIdsSequentially() {

        // Arrange: valid questions (matching UI validation rules)
        Question q1 = new Question(
                "What is 1+1?",
                new String[]{"2", "3", "4", "5"},
                0,
                QuestionDifficulty.EASY
        );

        Question q2 = new Question(
                "Capital of France?",
                new String[]{"London", "Paris", "Berlin", "Madrid"},
                1,
                QuestionDifficulty.MEDIUM
        );

        Question q3 = new Question(
                "Largest ocean?",
                new String[]{"Atlantic", "Indian", "Pacific", "Arctic"},
                2,
                QuestionDifficulty.HARD
        );

        // Persist questions to CSV
        assertTrue(SysData.addQuestion(q1));
        assertTrue(SysData.addQuestion(q2));
        assertTrue(SysData.addQuestion(q3));

        // Act: delete middle question
        assertTrue(SysData.deleteQuestion(q2));
        List<Question> remaining = SysData.loadQuestions();

        // Assert: size reduced and IDs reassigned sequentially
        assertEquals(2, remaining.size());
        assertEquals(1, remaining.get(0).getId());
        assertEquals(2, remaining.get(1).getId());
    }

    /**
     * White-box test:
     * Verifies that addQuestion assigns sequential IDs
     * based on internal CSV state.
     */
    @Test
    void testAddQuestion_AssignsSequentialIds() {

        // Arrange
        Question q1 = new Question(
                "Question one?",
                new String[]{"A", "B", "C", "D"},
                0,
                QuestionDifficulty.EASY
        );

        Question q2 = new Question(
                "Question two?",
                new String[]{"A2", "B2", "C2", "D2"},
                1,
                QuestionDifficulty.MEDIUM
        );

        Question q3 = new Question(
                "Question three?",
                new String[]{"A3", "B3", "C3", "D3"},
                2,
                QuestionDifficulty.HARD
        );

        // Act: add questions sequentially
        SysData.addQuestion(q1);
        SysData.addQuestion(q2);
        SysData.addQuestion(q3);

        List<Question> questions = SysData.loadQuestions();

        // Assert: IDs are assigned incrementally starting from 1
        assertEquals(3, questions.size());
        assertEquals(1, questions.get(0).getId());
        assertEquals(2, questions.get(1).getId());
        assertEquals(3, questions.get(2).getId());
    }
}
