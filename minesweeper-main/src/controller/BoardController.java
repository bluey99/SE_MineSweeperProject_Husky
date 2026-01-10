package controller;

import model.Board;
import model.Cell;
import model.GameModel;
import service.RevealResult;
import service.RevealService;

public class BoardController {

    // ------------------ "Singleton per player" instances ------------------
    private static BoardController instanceP1;
    private static BoardController instanceP2;

    public static BoardController getInstance(int playerNum,
                                             GameController gameController,
                                             GameModel gameModel,
                                             Board logicalBoard,
                                             CellController[][] uiBoard,
                                             RevealService revealService) {

        if (playerNum != 1 && playerNum != 2) {
            throw new IllegalArgumentException("playerNum must be 1 or 2");
        }

        BoardController current = (playerNum == 1) ? instanceP1 : instanceP2;

        if (current == null) {
            current = new BoardController(playerNum, gameController, gameModel, logicalBoard, uiBoard, revealService);
            if (playerNum == 1) instanceP1 = current;
            else instanceP2 = current;

            return current;
        }

        if (current.gameController != gameController ||
            current.gameModel != gameModel ||
            current.logicalBoard != logicalBoard ||
            current.uiBoard != uiBoard ||
            current.revealService != revealService) {
            throw new IllegalStateException(
                "BoardController instance for player " + playerNum +
                " already exists with different constructor parameters. " +
                "Don't create it twice; reuse the existing instance."
            );
        }

        return current;
    }

    public static BoardController getInstance(int playerNum) {
        if (playerNum != 1 && playerNum != 2) {
            throw new IllegalArgumentException("playerNum must be 1 or 2");
        }
        BoardController current = (playerNum == 1) ? instanceP1 : instanceP2;
        if (current == null) {
            throw new IllegalStateException(
                "BoardController for player " + playerNum + " not created yet. " +
                "Call getInstance(...params...) first."
            );
        }
        return current;
    }

    public static void resetInstances() {
        instanceP1 = null;
        instanceP2 = null;
    }

    // ------------------ Fields ------------------

    private final int playerNum;
    private final GameController gameController;
    private final GameModel gameModel;

    private final Board logicalBoard;
    private final CellController[][] uiBoard;
    private final RevealService revealService;

    private final int totalMinesOnBoard;

    private int correctFlagCount = 0;
    private int openedMineCount = 0;

    private BoardController(int playerNum,
                            GameController gameController,
                            GameModel gameModel,
                            Board logicalBoard,
                            CellController[][] uiBoard,
                            RevealService revealService) {
        this.playerNum = playerNum;
        this.gameController = gameController;
        this.gameModel = gameModel;
        this.logicalBoard = logicalBoard;
        this.uiBoard = uiBoard;
        this.revealService = revealService;

        this.totalMinesOnBoard = countMines(logicalBoard);
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public CellController[][] getUiBoard() {
        return uiBoard;
    }

    public Board getLogicalBoard() {
        return logicalBoard;
    }

    public int getMinesLeft() {
        int left = totalMinesOnBoard - (correctFlagCount + openedMineCount);
        return Math.max(left, 0);
    }

    public boolean applyMineGiftFlag(int row, int col) {
        if (row < 0 || col < 0 || row >= uiBoard.length || col >= uiBoard[0].length) return false;

        CellController cellCtrl = uiBoard[row][col];
        Cell cell = cellCtrl.getCell();

        if (cell.isOpen() || cell.isFlag()) return false;
        if (!cell.isMine()) return false;

        cell.setFlag();
        cell.setFlagScored(true);

        correctFlagCount++;

        cellCtrl.init();

        // Observer should refresh UI, but leaving these is ok if you want:
        gameController.updateUI();

        gameController.checkWinCondition();

        return true;
    }

    public void handleLeftClick(int row, int col) {
        if (!gameController.isGameActive()) return;
        if (gameController.getCurrentPlayer() != playerNum) return;

        CellController cellCtrl = uiBoard[row][col];
        Cell cell = cellCtrl.getCell();

        if (cell.isSurprise() && cell.isDiscovered() && !cell.isActivated()) {
            gameController.activateSurpriseCell(cellCtrl);
            return;
        }

        if (cell.isQuestion() && cell.isDiscovered() && !cell.isActivated()) {
            gameController.activateQuestionCell(cellCtrl);
            return;
        }

        if (cell.isOpen() || cell.isFlag()) return;

        RevealResult revealResult = revealService.revealCell(logicalBoard, gameModel, row, col, true);

        for (RevealResult.CellPos p : revealResult.getOpenedCells()) {
            uiBoard[p.row][p.col].init();
        }

        if (!gameController.isGameActive()) return;

        if (cell.isMine()) {
            if (cell.isOpen()) {
                openedMineCount++;
            }

            gameController.handleMineHit();
            if (gameController.isGameActive()) {
                gameController.switchPlayer();
            }

        } else {
            if (gameController.isGameActive()) {
                gameController.switchPlayer();
            }
        }

        // Observer should refresh UI, but leaving these is ok:
        gameController.updateUI();
        gameController.checkWinCondition();
    }

    public void handleRightClick(int row, int col) {
        if (!gameController.isGameActive()) return;
        if (gameController.getCurrentPlayer() != playerNum) return;

        CellController cellCtrl = uiBoard[row][col];
        Cell cell = cellCtrl.getCell();

        if (cell.isOpen()) return;

        boolean wasFlagged = cell.isFlag();
        cell.toggleFlag();
        boolean isFlagged = cell.isFlag();

        if (!wasFlagged && isFlagged) {
            if (cell.isMine()) correctFlagCount++;
        } else if (wasFlagged && !isFlagged) {
            if (cell.isMine()) correctFlagCount--;
        }

        // ✅ FIX: use GameModel methods so observers get notified
        if (!wasFlagged && isFlagged && !cell.isFlagScored()) {
            if (cell.isMine()) gameModel.addScore(+1);
            else               gameModel.addScore(-3);

            cell.setFlagScored(true);
        }

        cellCtrl.init();

        // Observer should refresh UI, but leaving this is ok:
        gameController.updateUI();

        gameController.checkWinCondition();
    }

    public void forceRevealAll() {
        RevealResult res = revealService.revealAllForce(logicalBoard);
        for (RevealResult.CellPos p : res.getOpenedCells()) {
            uiBoard[p.row][p.col].init();
        }
    }

    private int countMines(Board board) {
        int count = 0;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isMine()) count++;
            }
        }
        return count;
    }
}
