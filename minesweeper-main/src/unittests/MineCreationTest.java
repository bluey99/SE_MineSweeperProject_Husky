package unittests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import model.Board;
import model.Cell;
import model.GameModel;

public class MineCreationTest {

    private int countMines(Board b) {
        int mines = 0;
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                Cell cell = b.getCell(r, c);
                if (cell.isMine()) mines++; 
            }
        }
        return mines;
    }

    @Test
    void easyBoard_hasExactly10Mines_onBothBoards() {
        int N = 9, M = 9;
        int mineCount = 10;
        int sharedLives = 10;

        GameModel gm = new GameModel(null, mineCount, sharedLives);
        gm.initializeBoards(N, M);

        assertEquals(mineCount, countMines(gm.getBoard1()));
        assertEquals(mineCount, countMines(gm.getBoard2()));
    }

    @Test
    void hardBoard_hasExactly44Mines_onBothBoards() {
        int N = 16, M = 16;
        int mineCount = 44;
        int sharedLives = 6;

        GameModel gm = new GameModel(null, mineCount, sharedLives);
        gm.initializeBoards(N, M);

        assertEquals(mineCount, countMines(gm.getBoard1()));
        assertEquals(mineCount, countMines(gm.getBoard2()));
    }
}
