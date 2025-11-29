package model;

/**
 * Surprise cell (good/bad outcome).
 * In generation logic it is always placed on a 0-neighbor cell,
 * so neighborMines == 0.
 */
public class SurpriseCell extends Cell {

    public SurpriseCell(int row, int col, int neighborMines) {
        super(row, col, neighborMines);
    }

    @Override
    public CellType getType() {
        return CellType.SURPRISE;
    }
}
