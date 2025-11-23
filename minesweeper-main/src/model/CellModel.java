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
    public static final String surpriseImgURL = "img/star.png"; // Reuse star image
    public static final String questionImgURL = "img/clock.png"; // Reuse clock image
    
    public static String numberImgURL(int num) {
        return "img/" + num + ".png";
    }
    
    // Cell properties
    public int neighborMinesNum = 0;
    private boolean mine = false;
    private boolean surprise = false;  // Surprise cell (good/bad outcome)
    private boolean question = false;  // Question cell (trivia)
    private boolean open = false;
    private boolean flag = false;
    private boolean discovered = false;  // Special cell revealed but not yet activated
    private boolean activated = false;   // Special cell has been used
    
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
    
    public void setFlag() {
        this.flag = !this.flag;
    }
    
    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }
    
    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}