package unittests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import service.SpecialCellResult;
import service.SpecialCellService;

public class SpecialCellSurpriseScoreTest {

    @Test
    void surprise_returnsValidResultAndKeepsValuesLegal() {
        SpecialCellService service = new SpecialCellService(new Random(1));

        int initialScore = 20;
        int initialLives = 5;

        SpecialCellResult result =
                service.processSurprise("Easy", initialScore, initialLives);

        // Invariants that must always hold
        assertTrue(result.newScore >= 0, "newScore should never be negative");
        assertTrue(result.newLives >= 0, "newLives should never be negative");

        // If the surprise is allowed, at least one value must change
        if (result.allowed) {
            assertTrue(
                result.newScore != initialScore || result.newLives != initialLives,
                "If allowed, score or lives should change"
            );
        }
    }
}
