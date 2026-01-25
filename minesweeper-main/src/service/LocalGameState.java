package service;

import java.util.Base64;
import java.util.BitSet;
import java.util.Objects;

import model.Board;
import model.Cell;

/**
 * Offline-only snapshot of a local game in progress.
 * NOTE: This is NOT persisted by SysData directly; SysData stores an opaque payload.
 */
public final class LocalGameState {

    public static final int VERSION = 1;

    public String difficulty;
    public String p1Name;
    public String p2Name;

    public long seed;
    public int currentPlayer;   // 1 or 2
    public int sharedScore;
    public int sharedLives;
    public int elapsedTimeSec;

    public BoardBits board1;
    public BoardBits board2;

    public static final class BoardBits {
        public int rows;
        public int cols;

        public BitSet open = new BitSet();
        public BitSet flag = new BitSet();
        public BitSet discovered = new BitSet();
        public BitSet activated = new BitSet();
        public BitSet flagScored = new BitSet();

        public int cellCount() {
            return rows * cols;
        }
    }

    // ---------------- Capture / Apply ----------------

    public static BoardBits captureBoard(Board board) {
        Objects.requireNonNull(board, "board");

        BoardBits bits = new BoardBits();
        bits.rows = board.getRows();
        bits.cols = board.getCols();
        int cols = bits.cols;

        for (int r = 0; r < bits.rows; r++) {
            for (int c = 0; c < bits.cols; c++) {
                int idx = r * cols + c;
                Cell cell = board.getCell(r, c);

                if (cell.isOpen()) bits.open.set(idx);
                if (cell.isFlag()) bits.flag.set(idx);
                if (cell.isDiscovered()) bits.discovered.set(idx);
                if (cell.isActivated()) bits.activated.set(idx);
                if (cell.isFlagScored()) bits.flagScored.set(idx);
            }
        }

        return bits;
    }

    public static void applyBoard(Board board, BoardBits bits) {
        Objects.requireNonNull(board, "board");
        Objects.requireNonNull(bits, "bits");

        if (board.getRows() != bits.rows || board.getCols() != bits.cols) {
            throw new IllegalArgumentException("Board size mismatch. Expected "
                    + bits.rows + "x" + bits.cols + " but got "
                    + board.getRows() + "x" + board.getCols());
        }

        int cols = bits.cols;

        for (int r = 0; r < bits.rows; r++) {
            for (int c = 0; c < bits.cols; c++) {
                int idx = r * cols + c;
                Cell cell = board.getCell(r, c);

                boolean isOpen = bits.open.get(idx);
                boolean isFlag = bits.flag.get(idx);

                // Rule: opened cell must not remain flagged
                cell.setOpen(isOpen);
                if (!isOpen) {
                    if (isFlag) cell.setFlag();
                    else cell.clearFlag();
                }

                cell.setDiscovered(bits.discovered.get(idx));
                cell.setActivated(bits.activated.get(idx));
                cell.setFlagScored(bits.flagScored.get(idx));
            }
        }
    }

    // ---------------- BitSet encoding helpers ----------------

    public static String bitsetToBase64(BitSet bs) {
        if (bs == null) return "";
        byte[] bytes = bs.toByteArray();
        if (bytes.length == 0) return "";
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static BitSet base64ToBitset(String b64) {
        BitSet bs = new BitSet();
        if (b64 == null) return bs;

        String s = b64.trim();
        if (s.isEmpty()) return bs;

        byte[] bytes = Base64.getDecoder().decode(s);
        return BitSet.valueOf(bytes);
    }
}
