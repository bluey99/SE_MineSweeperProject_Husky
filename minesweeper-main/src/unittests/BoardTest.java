package unittests;

import model.Board;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BoardTest {

    @Test
    public void testBoardSize() {
        Board board = new Board(5, 7);

        assertEquals(5, board.getRows());
        assertEquals(7, board.getCols());
    }
}
