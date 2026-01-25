package multiplayer;

import java.io.Serializable;

public class GameSettingsPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String difficulty;
    public final long seed;

    public GameSettingsPayload(String difficulty, long seed) {
        this.difficulty = difficulty;
        this.seed = seed;
    }

    @Override
    public String toString() {
        return "GameSettingsPayload{difficulty='" + difficulty + "', seed=" + seed + "}";
    }
}
