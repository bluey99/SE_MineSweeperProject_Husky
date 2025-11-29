package model;

/**
 * Logical board for one player.
 * Holds a 2D grid of Cell objects and exposes basic helpers.
 */
public class Board {

    private final int rows;
    private final int cols;
    private final Cell[][] cells;

    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new Cell[rows][cols];
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

    public void setCell(int row, int col, Cell cell) {
        cells[row][col] = cell;
    }
}
