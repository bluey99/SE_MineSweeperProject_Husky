package model;

import controller.GameController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Game model for cooperative two-player Minesweeper.
 *
 * Observer version:
 * - GameModel notifies observers when sharedScore/sharedLives change.
 */
public class GameModel {

    // Optional back-reference to controller (kept for compatibility)
    @SuppressWarnings("unused")
    private final GameController controller;

    // Two logical boards (one per player)
    private Board board1;
    private Board board2;

    // ---------------- Observer ----------------
    private final List<GameModelObserver> observers = new ArrayList<>();

    public void addObserver(GameModelObserver o) {
        if (o != null && !observers.contains(o)) observers.add(o);
    }

    public void removeObserver(GameModelObserver o) {
        observers.remove(o);
    }

    private void notifyObservers() {
        for (GameModelObserver o : observers) {
            o.onGameModelChanged();
        }
    }

    // ---------------- Shared game state ----------------
    // 3.2.37 – shared cumulative score, starts from 0
    private int sharedScore = 0;
    private int sharedLives;        // 3.2.36 – initialized by difficulty
    public int revealedCells = 0;   // can stay public for now (optional)

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

    // ---------------- Getters (used by GameController.updateUI) ----------------
    public int getSharedScore() {
        return sharedScore;
    }

    public int getSharedLives() {
        return sharedLives;
    }

    // ---------------- Mutators that notify ----------------
    public void setSharedScore(int value) {
        this.sharedScore = value;
        notifyObservers();
    }

    public void addScore(int delta) {
        this.sharedScore += delta;
        notifyObservers();
    }

    public void setSharedLives(int value) {
        this.sharedLives = value;
        notifyObservers();
    }

    public void addLives(int delta) {
        this.sharedLives += delta;
        notifyObservers();
    }

    // ---------------- Public API ----------------
    public Board getBoard1() {
        return board1;
    }

    public Board getBoard2() {
        return board2;
    }

    /**
     * Initialize both boards and reset shared state for a new game.
     */
    public void initializeBoards(int rows, int cols) {
        setSharedScore(0);              // notify
        revealedCells = 0;
        setSharedLives(initialLives);   // notify

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

        // 4) Build the Board <<<<<FACTORY DESIGN PATTERN>>>>>
        Board board = new Board(rows, cols);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

            	CellType type;

            	if (mines[r][c]) {
            	    type = CellType.MINE;
            	} else if (surpriseMask[r][c]) {
            	    type = CellType.SURPRISE;
            	} else if (questionMask[r][c]) {
            	    type = CellType.QUESTION;
            	} else {
            	    type = CellType.NORMAL;
            	}

            	Cell cell = CellFactory.createCell(type, r, c, neighborMines[r][c]);
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
                    neighborMines[r][c] = -1;
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

    private void selectSpecialCells(boolean[][] mines,
                                    int[][] neighborMines,
                                    boolean[][] surpriseMask,
                                    boolean[][] questionMask,
                                    int rows, int cols) {

        List<int[]> emptyZero = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!mines[r][c] && neighborMines[r][c] == 0) {
                    emptyZero.add(new int[]{r, c});
                }
            }
        }

        if (emptyZero.isEmpty()) return;

        Collections.shuffle(emptyZero);

        int surpriseCount = Math.max(2, (int) (emptyZero.size() * surpriseRate));
        int questionCount = Math.max(3, (int) (emptyZero.size() * questionRate));

        int idx = 0;

        for (int i = 0; i < surpriseCount && idx < emptyZero.size(); i++, idx++) {
            int[] pos = emptyZero.get(idx);
            surpriseMask[pos[0]][pos[1]] = true;
        }

        for (int i = 0; i < questionCount && idx < emptyZero.size(); i++, idx++) {
            int[] pos = emptyZero.get(idx);
            questionMask[pos[0]][pos[1]] = true;
        }
    }

    private boolean inBounds(int r, int c, int rows, int cols) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }
}
