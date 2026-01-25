package multiplayer;

import java.io.Serializable;

public class QuestionResultPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int playerNum;     // 1 or 2 (board owner)
    public final int row;
    public final int col;
    public final boolean correct;
    public final String qDiffLabel;

    public QuestionResultPayload(int playerNum, int row, int col, boolean correct, String qDiffLabel) {
        this.playerNum = playerNum;
        this.row = row;
        this.col = col;
        this.correct = correct;
        this.qDiffLabel = qDiffLabel;
    }

    @Override
    public String toString() {
        return "QuestionResultPayload{playerNum=" + playerNum +
                ", row=" + row + ", col=" + col +
                ", correct=" + correct +
                ", qDiffLabel='" + qDiffLabel + "'}";
    }
}
