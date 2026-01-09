package controller;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Cell;
import view.CellView;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
/**
 * Cell controller - connects one logical Cell (model) to its CellView (UI).
 * Renders cells with JavaFX styles in a modern Minesweeper style.
 */
public class CellController {

    // Logical cell (from model package)
    private final Cell cell;

    // UI node
    public final CellView cellView;

    // Size of each cell – GameController sets this per difficulty
    private static int CELL_SIDE = 26;
    // board-specific tint (player color)
    private Color boardTint = null;

    // setter from GameController
    public void setBoardTint(Color tint) {
        this.boardTint = tint;
    }


    public static void setCellSide(int size) {
        CELL_SIDE = size;
    }

    public static int getCellSide() {
        return CELL_SIDE;
    }

    public CellController(Cell cell) {
        this.cell = cell;
        this.cellView = new CellView(this);
        init();
    }

    public Cell getCell() {
        return cell;
    }

    public void init() {
        cellView.getChildren().clear();

        if (cell.isOpen()) {
            drawOpenCell();
        } else {
            drawClosedCell();
        }
    }

    // ---------------------------------------------------------------------
    // CLOSED / FLAGGED
    // ---------------------------------------------------------------------
    private void drawClosedCell() {

        //  use board tint if provided
        Color base = (boardTint != null) ? boardTint : Color.web("#65A30D");

        String baseStyle =
                "-fx-background-color: linear-gradient(" +
                toCssColor(base.brighter()) + ", " +
                toCssColor(base.darker()) + ");" +
                "-fx-border-color: #365314;" +
                "-fx-border-width: 1;" +
                "-fx-background-radius: 6;" +
                "-fx-border-radius: 6;";

        cellView.setStyle(baseStyle);

        if (cell.isFlag()) {
            Label flag = new Label("⚑"); // nicer than emoji 🚩
            flag.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, CELL_SIDE * 0.65));
            flag.setTextFill(Color.web("#38BDF8")); // cyan

            // shadow so it pops everywhere
            flag.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.95), 3, 0.7, 0, 0);");

            // badge behind the flag
            StackPane badge = new StackPane(flag);
            badge.setStyle(
                    "-fx-background-color: rgba(2,6,23,0.75);" +   // dark badge
                    "-fx-background-radius: 999;" +
                    "-fx-border-color: rgba(56,189,248,0.55);" +  // cyan border
                    "-fx-border-width: 1.2;" +
                    "-fx-border-radius: 999;"
            );
            badge.setPadding(new Insets(1.5, 4.5, 1.5, 4.5));

            cellView.getChildren().add(badge);
        }
    }


    // ---------------------------------------------------------------------
    // OPEN STATES
    // ---------------------------------------------------------------------
    private void drawOpenCell() {
        // Base open style: dark tile
    	String openStyle =
    	        "-fx-background-color: #020617;" +
    	        "-fx-border-color: #3E4C69;" +    // <-- brighter grid line
    	        "-fx-border-width: 1.2;" +
    	        "-fx-background-radius: 6;" +
    	        "-fx-border-radius: 6;";


        if (cell.isMine()) {
            Label mine = makeLabel("💣", Color.web("#FB7185"));
            cellView.setStyle(openStyle);
            cellView.getChildren().add(mine);
            return;
        }

        // Surprise cell
        if (cell.isSurprise()) {
            if (cell.isActivated()) {
                // After activation – treat as empty (0)
                drawNumberCell(0, openStyle);
            } else {
                Label star = makeLabel("★", Color.web("#FACC15")); // bright yellow
                cellView.setStyle(
                        openStyle +
                        "-fx-border-color: #FACC15;" +
                        "-fx-border-width: 2;"
                );
                cellView.getChildren().add(star);
            }
            return;
        }

        // Question cell
        if (cell.isQuestion()) {
            if (cell.isActivated()) {
                // After activation – treat as empty (0)
                drawNumberCell(0, openStyle);
            } else {
                // VERY visible: pink ? and pink border
                Label q = makeLabel("?", Color.web("#EC4899"));
                cellView.setStyle(
                        openStyle +
                        "-fx-border-color: #EC4899;" +
                        "-fx-border-width: 2;"
                );
                cellView.getChildren().add(q);
            }
            return;
        }

        // Normal numbered cell
        drawNumberCell(cell.getNeighborMinesNum(), openStyle);
    }

    private void drawNumberCell(int number, String style) {
        cellView.setStyle(style);

        if (number <= 0) {
            // 0 neighbors – just an empty dark tile
            return;
        }

        Color color = getNumberColor(number);
        Label lbl = makeLabel(String.valueOf(number), color);
        cellView.getChildren().add(lbl);
    }

    // ---------------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------------
    private Label makeLabel(String text, Color color) {
        Label lbl = new Label(text);
        double fontSize = CELL_SIDE * 0.6;
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        lbl.setTextFill(color);
        return lbl;
    }

    /**
     * Choose a color for the neighbor number.
     * 1 is blue (not pink), so it doesn't clash with question cells.
     */
    private Color getNumberColor(int n) {
        switch (n) {
            case 1:  return Color.web("#3B82F6"); // blue
            case 2:  return Color.web("#22C55E"); // green
            case 3:  return Color.web("#FACC15"); // yellow
            case 4:  return Color.web("#FB7185"); // red/pink
            case 5:  return Color.web("#F97316"); // orange
            case 6:  return Color.web("#22D3EE"); // cyan
            case 7:  return Color.web("#A855F7"); // purple
            case 8:  return Color.web("#E5E7EB"); // light gray
            default: return Color.web("#E5E7EB");
        }
    }
    
 // convert JavaFX Color to CSS rgb
    private String toCssColor(Color c) {
        return String.format(
                "rgb(%d,%d,%d)",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255)
        );
    }

}
