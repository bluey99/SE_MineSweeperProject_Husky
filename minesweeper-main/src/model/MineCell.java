package model;

/**
 * A mine cell. neighborMines is always -1 by convention.
 */
public class MineCell extends Cell {

    public MineCell(int row, int col) {
        super(row, col, -1);
    }

    @Override
    public CellType getType() {
        return CellType.MINE;
    }
}
