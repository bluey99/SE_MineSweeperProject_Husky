package model;

/**
 * Factory for creating Cell objects.
 * Centralizes cell creation logic and avoids scattered if/else instantiation.
 */
public final class CellFactory {

    private CellFactory() {} // prevent instantiation

    public static Cell createCell(CellType type, int row, int col, int neighborMines) {
        switch (type) {
            case MINE:
                return new MineCell(row, col);

            case SURPRISE:
                return new SurpriseCell(row, col, neighborMines);

            case QUESTION:
                return new QuestionCell(row, col, neighborMines);

            case NORMAL:
            default:
                return new NormalCell(row, col, neighborMines);
        }
    }
}
