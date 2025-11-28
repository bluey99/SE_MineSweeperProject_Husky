package model;

import controller.CellController;

/**
 * Cell model for cooperative two-player Minesweeper
 * Supports: mines, numbers, empty cells, Surprise cells, and Question cells
 */
public class CellModel {
    private CellController cellController;
    
    // Image URLs
    public static final String coverImgURL = "img/cover.png";
    public static final String flagImgURL = "img/flag.png";
    public static final String mineImgURL = "img/mine.png";
    public static final String surpriseImgURL = "img/SurpriseIcon.png";     // Surprise icon
    public static final String questionImgURL = "img/QuestionIcon.png";    // Question icon
    
    public static String numberImgURL(int num) {
        return "img/" + num + ".png";
    }
    
    // Cell properties
    public int neighborMinesNum = 0;
    private boolean mine = false;
    private boolean surprise = false;     // Surprise cell (good/bad outcome)
    private boolean question = false;     // Question cell (trivia)
    private boolean open = false;
    private boolean flag = false;
    private boolean discovered = false;   // Special cell revealed but not yet activated
    private boolean activated = false;    // Special cell has been used

    // NEW: has this cell already affected score due to flagging?
    private boolean flagScored = false;
    
    public int cellSide = 32;
    
    public CellModel(CellController cellController) {
        this.cellController = cellController;
    }
    
    // Getters
    public int getNeighborMinesNum() {
        return neighborMinesNum;
    }
    
    public boolean isMine() {
        return mine;
    }
    
    public boolean isSurprise() {
        return surprise;
    }
    
    public boolean isQuestion() {
        return question;
    }
    
    public boolean isOpen() {
        return open;
    }
    
    public boolean isFlag() {
        return flag;
    }
    
    public boolean isDiscovered() {
        return discovered;
    }
    
    public boolean isActivated() {
        return activated;
    }

    public boolean isFlagScored() {
        return flagScored;
    }
    
    // Setters
    public void setMine(boolean mine) {
        this.mine = mine;
    }
    
    public void setSurprise(boolean surprise) {
        this.surprise = surprise;
    }
    
    public void setQuestion(boolean question) {
        this.question = question;
    }
    
    public void setOpen(boolean open) {
        this.open = open;
    }
    
    /**
     * Toggle flag state on/off.
     * (Used by GameController for right-click handling)
     */
    public void toggleFlag() {
        this.flag = !this.flag;
    }

    /**
     * Kept for backward compatibility with existing code.
     * Internally just toggles the flag.
     */
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
}
