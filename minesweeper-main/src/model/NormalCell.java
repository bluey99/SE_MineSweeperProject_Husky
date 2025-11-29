package model;

/**
 * Standard non-special cell.
 * Holds a neighbor mine count 0–8.
 */
public class NormalCell extends Cell {

    public NormalCell(int row, int col, int neighborMines) {
        super(row, col, neighborMines);
    }

    /** Convenience: true if there are no neighboring mines. */
    public boolean isEmpty() {
        return neighborMines == 0;
    }

    @Override
    public CellType getType() {
        return CellType.NORMAL;
    }
}
