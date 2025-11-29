package controller;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Cell;
import view.CellView;

/**
 * Cell controller - connects one logical Cell (model) to its CellView (UI).
 * It decides which image to show based on the Cell state and type.
 */
public class CellController {

    // Logical cell (from model package)
    private final Cell cell;

    // UI node
    public final CellView cellView;

    // Size of each cell image (replaces old cellModel.cellSide)
    public static final int CELL_SIDE = 32;

    public CellController(Cell cell) {
        this.cell = cell;
        this.cellView = new CellView(this);
        init();
    }

    /** Expose the underlying logical cell to the GameController. */
    public Cell getCell() {
        return cell;
    }

    /**
     * Refresh the visual appearance based on the cell state.
     * Called at construction time and after each state change.
     */
    public void init() {
        cellView.getChildren().clear();

        if (cell.isOpen()) {
            // Cell is opened - show its content
            if (cell.isMine()) {
                // Show mine
                cellView.getChildren().add(drawImg(
                        CELL_SIDE,
                        CELL_SIDE,
                        Cell.MINE_IMG_URL
                ));
            } else if (cell.isSurprise()) {
                if (cell.isActivated()) {
                    // Surprise used - show as empty (0)
                    cellView.getChildren().add(drawImg(
                            CELL_SIDE,
                            CELL_SIDE,
                            Cell.numberImgURL(0)
                    ));
                } else {
                    // Show surprise icon (can be activated)
                    cellView.getChildren().add(drawImg(
                            CELL_SIDE,
                            CELL_SIDE,
                            Cell.SURPRISE_IMG_URL
                    ));
                }
            } else if (cell.isQuestion()) {
                if (cell.isActivated()) {
                    // Question answered - show as empty (0)
                    cellView.getChildren().add(drawImg(
                            CELL_SIDE,
                            CELL_SIDE,
                            Cell.numberImgURL(0)
                    ));
                } else {
                    // Show question icon (can be activated)
                    cellView.getChildren().add(drawImg(
                            CELL_SIDE,
                            CELL_SIDE,
                            Cell.QUESTION_IMG_URL
                    ));
                }
            } else {
                // Show number (0-8 neighboring mines)
                cellView.getChildren().add(drawImg(
                        CELL_SIDE,
                        CELL_SIDE,
                        Cell.numberImgURL(cell.getNeighborMinesNum())
                ));
            }
        } else {
            // Cell is not opened yet
            if (cell.isFlag()) {
                // Show flag
                cellView.getChildren().add(drawImg(
                        CELL_SIDE,
                        CELL_SIDE,
                        Cell.FLAG_IMG_URL
                ));
            } else {
                // Show cover
                cellView.getChildren().add(drawImg(
                        CELL_SIDE,
                        CELL_SIDE,
                        Cell.COVER_IMG_URL
                ));
            }
        }
    }

    private ImageView drawImg(int width, int height, String imgURL) {
        Image img = new Image(imgURL);
        ImageView imgView = new ImageView(img);
        imgView.setFitHeight(height);
        imgView.setFitWidth(width);
        return imgView;
    }
}
