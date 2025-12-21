package unittests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import service.SpecialCellResult;
import service.SpecialCellService;

public class SpecialCellSurpriseScoreTest {

    @Test
    void surpriseEasy_updatesScoreAndLivesCorrectly() {
        SpecialCellService service = new SpecialCellService(new Random(1));

        int initialScore = 20;
        int initialLives = 5;

        SpecialCellResult result =
                service.processSurprise("Easy", initialScore, initialLives);

        // Expectation: the event is allowed (should not be blocked)
        assertTrue(result.allowed, "Expected Surprise event to be allowed for Easy with this seed");

        // Expectation: returned score/lives represent the updated state (must be valid updates)
        assertTrue(result.newScore >= 0, "newScore should never be negative");
        assertTrue(result.newLives >= 0, "newLives should never be negative");

        // Basic sanity: in an allowed surprise, something should change (score or lives)
        assertTrue(
                result.newScore != initialScore || result.newLives != initialLives,
                "Allowed Surprise should change score or lives"
        );
    }

    @Test
    void surpriseHard_doesNotProduceNegativeLives() {
        SpecialCellService service = new SpecialCellService(new Random(2));

        SpecialCellResult result =
                service.processSurprise("Hard", 10, 1);

        assertTrue(result.newLives >= 0, "Lives should never become negative");
    }
}
