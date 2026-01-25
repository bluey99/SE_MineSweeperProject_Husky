// multiplayer/GameSettings.java
package multiplayer;

import java.io.Serializable;

public class GameSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String hostName;
    public final String joinName;
    public final String difficulty;
    public final long seed;

    public GameSettings(String hostName, String joinName, String difficulty, long seed) {
        this.hostName = hostName;
        this.joinName = joinName;
        this.difficulty = difficulty;
        this.seed = seed;
    }

    // ✅ Convenience: use when syncing NEW_GAME seed/difficulty only
    public static GameSettings forNewGame(String difficulty, long seed) {
        return new GameSettings("", "", difficulty, seed);
    }
}
