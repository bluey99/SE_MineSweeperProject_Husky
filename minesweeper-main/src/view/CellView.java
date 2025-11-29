package view;

import controller.CellController;
import javafx.scene.layout.StackPane;

/**
 * Cell view - visual representation of a cell.
 * The CellController decides which image is shown.
 */
public class CellView extends StackPane {

    private final CellController cellController;

    public CellView(CellController cellController) {
        this.cellController = cellController;
    }

    public CellController getCellController() {
        return cellController;
    }
}
