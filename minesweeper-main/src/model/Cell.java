package model;

/**
 * Base logical cell on the Minesweeper board.
 *
 * It holds:
 *  - position (row, col)
 *  - neighbor mines count
 *  - open / flag state
 *  - special status flags (discovered / activated / flagScored)
 *
 * Specific kinds of cells (mine, surprise, question, normal)
 * are implemented as subclasses.
 */
public abstract class Cell {

    // ---- Shared image paths ----
    public static final String COVER_IMG_URL    = "img/cover.png";
    public static final String FLAG_IMG_URL     = "img/flag.png";
    public static final String MINE_IMG_URL     = "img/mine.png";
    public static final String SURPRISE_IMG_URL = "img/SurpriseIcon.png";
    public static final String QUESTION_IMG_URL = "img/QuestionIcon.png";

    public static String numberImgURL(int num) {
        return "img/" + num + ".png";
    }

    protected final int row;
    protected final int col;

    protected int neighborMines;

    protected boolean open       = false;
    protected boolean flag       = false;
    protected boolean discovered = false;
    protected boolean activated  = false;
    protected boolean flagScored = false;

    protected Cell(int row, int col, int neighborMines) {
        this.row = row;
        this.col = col;
        this.neighborMines = neighborMines;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public int getNeighborMinesNum() {
        return neighborMines;
    }

    public boolean isOpen()       { return open; }
    public boolean isFlag()       { return flag; }
    public boolean isDiscovered() { return discovered; }
    public boolean isActivated()  { return activated; }
    public boolean isFlagScored() { return flagScored; }

    public void setOpen(boolean open) {
        this.open = open;
        if (open) {
            // When opened, it's usually safest to remove flag to avoid weird states.
            // If your rules allow "open+flag", remove this.
            this.flag = false;
        }
    }

    /** Toggle flag on/off (used on right-click). */
    public void toggleFlag() {
        if (!open) {
            this.flag = !this.flag;
        }
    }

    /**
     * Backwards-compatibility with old CellModel.setFlag().
     * FIX: This should NOT toggle. It should set flag = true deterministically.
     */
    public void setFlag() {
        if (!open) {
            this.flag = true;
        }
    }

    /** Utility if you want to unflag deterministically. */
    public void clearFlag() {
        if (!open) {
            this.flag = false;
        }
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setFlagScored(boolean flagScored) {
        this.flagScored = flagScored;
    }

    public boolean isMine()     { return this instanceof MineCell; }
    public boolean isSurprise() { return this instanceof SurpriseCell; }
    public boolean isQuestion() { return this instanceof QuestionCell; }

    public boolean isSpecial() {
        return isSurprise() || isQuestion();
    }

    public abstract CellType getType();
}
