package model;

import controller.CellController;
import controller.GameController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Game model for cooperative two-player Minesweeper
 * Manages two boards with shared score and lives
 */
public class GameModel {
    private GameController controller;
    
    // Two separate boards
    public CellController[][] board1;
    public CellController[][] board2;
    
    // Shared game state (both players contribute to same pool)
    public int sharedScore = 0;
    public int sharedLives;
    public int revealedCells = 0;
    
    // Game parameters
    private int mineCount;
    private double surpriseRate = 0.05; // 5% of empty cells
    private double questionRate = 0.08; // 8% of empty cells
    
    public GameModel(GameController controller, int mineCount, int initialLives) {
        this.controller = controller;
        this.mineCount = mineCount;
        this.sharedLives = initialLives;
    }
    
    public void initializeBoards(int N, int M) {
        sharedScore = 0;
        revealedCells = 0;
        
        // Initialize both boards
        board1 = new CellController[N][M];
        board2 = new CellController[N][M];
        
        // Setup board 1
        setupBoard(board1, N, M);
        
        // Setup board 2
        setupBoard(board2, N, M);
    }
    
    private void setupBoard(CellController[][] board, int N, int M) {
        // Create all cells
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                board[i][j] = new CellController();
            }
        }
        
        // Place mines randomly
        placeMines(board, N, M, mineCount);
        
        // Calculate neighbor mine numbers
        calculateNeighborMines(board, N, M);
        
        // Place special cells (only on empty cells with 0 neighbors)
        placeSpecialCells(board, N, M);
        
        // Initialize all cell views
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                board[i][j].init();
            }
        }
    }
    
    private void placeMines(CellController[][] board, int N, int M, int count) {
        Random rand = new Random();
        int placed = 0;
        
        while (placed < count) {
            int row = rand.nextInt(N);
            int col = rand.nextInt(M);
            
            if (!board[row][col].cellModel.isMine()) {
                board[row][col].cellModel.setMine(true);
                placed++;
            }
        }
    }
    
    private void calculateNeighborMines(CellController[][] board, int N, int M) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (board[i][j].cellModel.isMine()) {
                    board[i][j].cellModel.neighborMinesNum = -1;
                    continue;
                }
                
                int count = 0;
                for (int di = -1; di <= 1; di++) {
                    for (int dj = -1; dj <= 1; dj++) {
                        int ni = i + di;
                        int nj = j + dj;
                        
                        if (ni >= 0 && ni < N && nj >= 0 && nj < M && 
                            board[ni][nj].cellModel.isMine()) {
                            count++;
                        }
                    }
                }
                
                board[i][j].cellModel.neighborMinesNum = count;
            }
        }
    }
    
    private void placeSpecialCells(CellController[][] board, int N, int M) {
        // Find all empty cells (0 neighbors, not mines)
        ArrayList<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (!board[i][j].cellModel.isMine() && 
                    board[i][j].cellModel.getNeighborMinesNum() == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }
        
        // Shuffle empty cells for random placement
        Collections.shuffle(emptyCells);
        
        // Place Surprise Cells
        int surpriseCount = Math.max(2, (int)(emptyCells.size() * surpriseRate));
        for (int k = 0; k < Math.min(surpriseCount, emptyCells.size()); k++) {
            int[] pos = emptyCells.get(k);
            board[pos[0]][pos[1]].cellModel.setSurprise(true);
        }
        
        // Place Question Cells (from remaining empty cells)
        int questionCount = Math.max(3, (int)(emptyCells.size() * questionRate));
        for (int k = surpriseCount; k < Math.min(surpriseCount + questionCount, emptyCells.size()); k++) {
            int[] pos = emptyCells.get(k);
            board[pos[0]][pos[1]].cellModel.setQuestion(true);
        }
    }
}