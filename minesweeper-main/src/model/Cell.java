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

    // ---- Shared image paths (moved from old CellModel) ----
    public static final String COVER_IMG_URL    = "img/cover.png";
    public static final String FLAG_IMG_URL     = "img/flag.png";
    public static final String MINE_IMG_URL     = "img/mine.png";
    public static final String SURPRISE_IMG_URL = "img/SurpriseIcon.png";
    public static final String QUESTION_IMG_URL = "img/QuestionIcon.png";

    public static String numberImgURL(int num) {
        return "img/" + num + ".png";
    }

    // ---- Basic position ----
    protected final int row;
    protected final int col;

    // -1 for mines, 0–8 otherwise
    protected int neighborMines;

    // ---- State flags (same semantics as old CellModel) ----
    protected boolean open       = false;
    protected boolean flag       = false;
    protected boolean discovered = false;   // special cell revealed but not activated
    protected boolean activated  = false;   // special cell used
    protected boolean flagScored = false;   // flag already affected score

    protected Cell(int row, int col, int neighborMines) {
        this.row = row;
        this.col = col;
        this.neighborMines = neighborMines;
    }

    // ---- Basic getters ----
    public int getRow() { return row; }
    public int getCol() { return col; }

    /** Same name as in old CellModel for easy migration. */
    public int getNeighborMinesNum() {
        return neighborMines;
    }

    public boolean isOpen()       { return open; }
    public boolean isFlag()       { return flag; }
    public boolean isDiscovered() { return discovered; }
    public boolean isActivated()  { return activated; }
    public boolean isFlagScored() { return flagScored; }

    // ---- State setters (same semantics as old CellModel) ----
    public void setOpen(boolean open) {
        this.open = open;
    }

    /** Toggle flag on/off (used on right-click). */
    public void toggleFlag() {
        if (!open) {
            this.flag = !this.flag;
        }
    }

    /** Backwards-compatibility with CellModel.setFlag(). */
    public void setFlag() {
        toggleFlag();
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

    // ---- Type helpers (replacing isMine/isSurprise/isQuestion booleans) ----
    public boolean isMine()     { return this instanceof MineCell; }
    public boolean isSurprise() { return this instanceof SurpriseCell; }
    public boolean isQuestion() { return this instanceof QuestionCell; }

    public boolean isSpecial() {
        return isSurprise() || isQuestion();
    }

    /** For UI / logic if you want to switch on type instead of instanceof. */
    public abstract CellType getType();
}
