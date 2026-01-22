package multiplayer;

import java.io.Serializable;

public class GameOverPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    public final boolean won;
    public final int lifeBonus;
    public final int finalScore;

    public GameOverPayload(boolean won, int lifeBonus, int finalScore) {
        this.won = won;
        this.lifeBonus = lifeBonus;
        this.finalScore = finalScore;
    }
}
