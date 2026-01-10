package service;

import model.Board;
import model.Cell;
import model.GameModel;

public class RevealService {

    public RevealResult revealCell(Board board, GameModel gameModel, int row, int col, boolean isRootClick) {
        RevealResult result = new RevealResult();
        revealCellInternal(board, gameModel, row, col, isRootClick, result);
        return result;
    }

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

    public RevealResult revealAll(Board board, GameModel gameModel) {
        return revealAllForce(board);
    }

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

        // ✅ FIX: Observer-safe score update
        if (!isMine) {
            gameModel.addScore(1);
        }

        // ✅ special cells SHOULD cascade
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
