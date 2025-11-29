package model;

/**
 * Question (trivia) cell.
 * Also placed only on 0-neighbor cells in GameModel.
 */
public class QuestionCell extends Cell {

    public QuestionCell(int row, int col, int neighborMines) {
        super(row, col, neighborMines);
    }

    @Override
    public CellType getType() {
        return CellType.QUESTION;
    }
}
