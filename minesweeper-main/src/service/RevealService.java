package service;

import model.Board;
import model.Cell;
import model.GameModel;

public class RevealService {

    /**
     * Reveals a single cell and performs cascade (flood-fill) if needed.
     * - No JavaFX / UI logic here.
     * - Mutates the model (Cell state + GameModel.revealedCells + score rules).
     */
    public RevealResult revealCell(Board board, GameModel gameModel, int row, int col, boolean isRootClick) {
        RevealResult result = new RevealResult();
        revealCellInternal(board, gameModel, row, col, isRootClick, result);
        return result;
    }

    /**
     * Force-reveal all cells on a board (used when game ends).
     *
     * IMPORTANT:
     * - Must NOT change revealedCells or score/lives.
     * - Used only for game-over final display.
     */
    public RevealResult revealAllForce(Board board) {
        RevealResult result = new RevealResult();
        int rows = board.getRows();
        int cols = board.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isOpen()) {
                    cell.setOpen(true);

                    if (cell.isSpecial() && !cell.isDiscovered()) {
                        cell.setDiscovered(true);
                    }

                    result.addOpened(r, c);
                }
            }
        }

        return result;
    }

    /**
     * Backward-compatible method (if your code still calls revealAll(board, gameModel)).
     * Uses safe force-reveal behavior.
     */
    public RevealResult revealAll(Board board, GameModel gameModel) {
        return revealAllForce(board);
    }

    // ---------------------------------------------------------------------
    // Internal recursive logic (matches your old openCell behavior)
    // ---------------------------------------------------------------------
    private void revealCellInternal(Board board, GameModel gameModel, int row, int col,
                                    boolean isRootClick, RevealResult result) {

        if (!board.isInBounds(row, col)) return;

        Cell cell = board.getCell(row, col);

        if (cell.isOpen() || cell.isFlag()) return;

        cell.setOpen(true);
        gameModel.revealedCells++;
        result.addOpened(row, col);

        boolean isMine = cell.isMine();
        boolean isSpecial = cell.isSpecial();
        int neighbors = cell.getNeighborMinesNum();

        if (isSpecial && !cell.isDiscovered()) {
            cell.setDiscovered(true);
        }

        // +1 only for root click on normal (non-mine, non-special)
        if (isRootClick && !isMine && !isSpecial) {
            gameModel.sharedScore += 1;
        }

        // ✅ REVERTED: special cells SHOULD cascade (as course incharge said)
        boolean shouldExpand = !isMine && (neighbors == 0 || isSpecial);

        if (shouldExpand) {
            for (int i = row - 1; i <= row + 1; i++) {
                for (int j = col - 1; j <= col + 1; j++) {
                    if (!(i == row && j == col)) {
                        revealCellInternal(board, gameModel, i, j, false, result);
                    }
                }
            }
        }
    }
}
