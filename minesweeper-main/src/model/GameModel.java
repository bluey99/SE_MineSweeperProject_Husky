package model;

import controller.GameController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Game model for cooperative two-player Minesweeper.
 *
 * This version is purely "logical":
 *  - Each player has a Board (grid of Cell objects).
 *  - Game rules (mines, neighbor counts, special cells) are the same as before.
 *
 * The controller/view layer will later map each Cell to a CellController/CellView.
 */
public class GameModel {

    // Optional back-reference to controller (kept for compatibility, not used here yet)
    @SuppressWarnings("unused")
    private final GameController controller;

    // Two logical boards (one per player)
    private Board board1;
    private Board board2;

    // Shared game state (same semantics as before)
    public int sharedScore = 0;
    public int sharedLives;
    public int revealedCells = 0;

    // Game parameters
    private final int mineCount;
    private final double surpriseRate = 0.05; // 5% of empty zero-neighbor cells
    private final double questionRate = 0.08; // 8% of empty zero-neighbor cells
    private final int initialLives;

    public GameModel(GameController controller, int mineCount, int initialLives) {
        this.controller = controller;
        this.mineCount = mineCount;
        this.initialLives = initialLives;
        this.sharedLives = initialLives;
    }

    // ---------------- Public API ----------------

    public Board getBoard1() {
        return board1;
    }

    public Board getBoard2() {
        return board2;
    }

    public void initializeBoards(int rows, int cols) {
        sharedScore = 0;
        revealedCells = 0;
        sharedLives = initialLives;

        board1 = generateBoard(rows, cols);
        board2 = generateBoard(rows, cols);
    }

    // ---------------- Board generation ----------------

    private Board generateBoard(int rows, int cols) {
        boolean[][] mines = new boolean[rows][cols];
        int[][] neighborMines = new int[rows][cols];

        // 1) Place mines randomly
        placeMinesRandomly(mines, rows, cols);

        // 2) Compute neighbor mine counts for all non-mine cells
        computeNeighborCounts(mines, neighborMines, rows, cols);

        // 3) Decide which 0-neighbor cells become Surprise / Question cells
        boolean[][] surpriseMask = new boolean[rows][cols];
        boolean[][] questionMask = new boolean[rows][cols];
        selectSpecialCells(mines, neighborMines, surpriseMask, questionMask, rows, cols);

        // 4) Build the Board with the appropriate Cell subclass in each position
        Board board = new Board(rows, cols);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                Cell cell;
                if (mines[r][c]) {
                    cell = new MineCell(r, c);
                } else if (surpriseMask[r][c]) {
                    // By construction neighborMines[r][c] == 0
                    cell = new SurpriseCell(r, c, neighborMines[r][c]);
                } else if (questionMask[r][c]) {
                    // By construction neighborMines[r][c] == 0
                    cell = new QuestionCell(r, c, neighborMines[r][c]);
                } else {
                    cell = new NormalCell(r, c, neighborMines[r][c]);
                }

                board.setCell(r, c, cell);
            }
        }

        return board;
    }

    private void placeMinesRandomly(boolean[][] mines, int rows, int cols) {
        Random rand = new Random();
        int placed = 0;

        while (placed < mineCount) {
            int row = rand.nextInt(rows);
            int col = rand.nextInt(cols);

            if (!mines[row][col]) {
                mines[row][col] = true;
                placed++;
            }
        }
    }

    private void computeNeighborCounts(boolean[][] mines, int[][] neighborMines,
                                       int rows, int cols) {

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (mines[r][c]) {
                    neighborMines[r][c] = -1; // same convention as old CellModel
                    continue;
                }

                int count = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;

                        int nr = r + dr;
                        int nc = c + dc;
                        if (inBounds(nr, nc, rows, cols) && mines[nr][nc]) {
                            count++;
                        }
                    }
                }
                neighborMines[r][c] = count;
            }
        }
    }

    /**
     * Selects Surprise and Question cells:
     *  - Only on cells that are NOT mines and have 0 neighboring mines.
     *  - surpriseRate and questionRate are the same as in your old GameModel.
     */
    private void selectSpecialCells(boolean[][] mines,
                                    int[][] neighborMines,
                                    boolean[][] surpriseMask,
                                    boolean[][] questionMask,
                                    int rows, int cols) {

        List<int[]> emptyZero = new ArrayList<>();

        // Gather all empty zero-neighbor cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!mines[r][c] && neighborMines[r][c] == 0) {
                    emptyZero.add(new int[]{r, c});
                }
            }
        }

        if (emptyZero.isEmpty()) {
            return; // no place for special cells
        }

        // Shuffle for random placement
        Collections.shuffle(emptyZero);

        int surpriseCount = Math.max(2, (int) (emptyZero.size() * surpriseRate));
        int questionCount = Math.max(3, (int) (emptyZero.size() * questionRate));

        int idx = 0;

        // Put surprises
        for (int i = 0; i < surpriseCount && idx < emptyZero.size(); i++, idx++) {
            int[] pos = emptyZero.get(idx);
            surpriseMask[pos[0]][pos[1]] = true;
        }

        // Put questions
        for (int i = 0; i < questionCount && idx < emptyZero.size(); i++, idx++) {
            int[] pos = emptyZero.get(idx);
            questionMask[pos[0]][pos[1]] = true;
        }
    }

    private boolean inBounds(int r, int c, int rows, int cols) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }
}
