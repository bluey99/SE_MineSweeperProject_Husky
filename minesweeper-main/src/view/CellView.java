package view;

import controller.CellController;
import javafx.scene.layout.StackPane;

/**
 * Cell view - visual representation of a cell
 */
public class CellView extends StackPane {
    private CellController cellController;
    
    public CellView(CellController cellController) {
        this.cellController = cellController;
    }
}