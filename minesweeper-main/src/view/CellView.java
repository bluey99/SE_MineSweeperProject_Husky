package view;

import controller.CellController;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

/**
 * Visual node of a single cell.
 * All styling is done in CellController.
 */
public class CellView extends StackPane {

    private final CellController cellController;

    public CellView(CellController cellController) {
        this.cellController = cellController;

        int side = CellController.getCellSide();
        setPrefSize(side, side);
        setMinSize(side, side);
        setMaxSize(side, side);
        setAlignment(Pos.CENTER);
    }

    public CellController getCellController() {
        return cellController;
    }
}
