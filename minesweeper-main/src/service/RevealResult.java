package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RevealResult {

    public static class CellPos {
        public final int row;
        public final int col;

        public CellPos(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    private final List<CellPos> openedCells = new ArrayList<>();

    public void addOpened(int row, int col) {
        openedCells.add(new CellPos(row, col));
    }

    public List<CellPos> getOpenedCells() {
        return Collections.unmodifiableList(openedCells);
    }

    public boolean isEmpty() {
        return openedCells.isEmpty();
    }
}