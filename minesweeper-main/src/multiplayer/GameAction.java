package multiplayer;

import java.io.Serializable;

public class GameAction implements Serializable {
    private static final long serialVersionUID = 1L;

    // Renamed from Kind -> Type (clearer protocol naming)
    public enum Type { LEFT_CLICK, RIGHT_CLICK }

    public final Type type;
    public final int playerNum; // 1 or 2
    public final int row;
    public final int col;

    public GameAction(Type type, int playerNum, int row, int col) {
        this.type = type;
        this.playerNum = playerNum;
        this.row = row;
        this.col = col;
    }
}
