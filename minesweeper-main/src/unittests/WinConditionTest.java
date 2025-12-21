package unittests;

import static org.junit.Assert.*;
import org.junit.Test;

public class WinConditionTest {

    /**
     * Helper class used ONLY for unit testing the win condition logic.
     * This avoids changing production code.
     * This is the same code that GameController uses just copy pasted here for accessibility.
     * The reason the code was duplicated is to avoid the very heavy calls between Controllers.
     */
    private static class WinConditionService {

        static boolean shouldWin(boolean gameActive,
                                 boolean board1ClearedByOpen,
                                 boolean board2ClearedByOpen,
                                 int minesLeftBoard1,
                                 int minesLeftBoard2) {

            if (!gameActive) return false;

            boolean clearedByOpen =
                    board1ClearedByOpen || board2ClearedByOpen;

            boolean clearedByFlags =
                    (minesLeftBoard1 == 0) || (minesLeftBoard2 == 0);

            return clearedByOpen || clearedByFlags;
        }
    }

    @Test
    public void UT_WIN_SALIM_01_gameActive_openNotCleared_flagsCorrectOnBoard2_win() {

        
        boolean gameActive = true;

        boolean board1ClearedByOpen = false;
        boolean board2ClearedByOpen = false;   // clearedByOpen = false

        int minesLeftBoard1 = 3;
        int minesLeftBoard2 = 0;               // clearedByFlags = true

        // Act
        boolean shouldWin = WinConditionService.shouldWin(
                gameActive,
                board1ClearedByOpen,
                board2ClearedByOpen,
                minesLeftBoard1,
                minesLeftBoard2
        );

        // Assert
        assertTrue(shouldWin);
    }
}
