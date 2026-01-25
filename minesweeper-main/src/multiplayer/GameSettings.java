package multiplayer;

import java.io.Serializable;

public class GameSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String hostName;
    public final String joinName;
    public final String difficulty;
    public final long seed;

    // optional UI sync fields (client can use them if present)
    public final int cellSide;      // -1 if not provided
    public final double windowW;    // -1 if not provided
    public final double windowH;    // -1 if not provided

    // ✅ old constructor (unchanged)
    public GameSettings(String hostName, String joinName, String difficulty, long seed) {
        this.hostName = hostName;
        this.joinName = joinName;
        this.difficulty = difficulty;
        this.seed = seed;
        this.cellSide = -1;
        this.windowW = -1;
        this.windowH = -1;
    }

    // ✅ new overload (optional)
    public GameSettings(String hostName, String joinName, String difficulty, long seed,
                        int cellSide, double windowW, double windowH) {
        this.hostName = hostName;
        this.joinName = joinName;
        this.difficulty = difficulty;
        this.seed = seed;
        this.cellSide = cellSide;
        this.windowW = windowW;
        this.windowH = windowH;
    }

    public static GameSettings forNewGame(String difficulty, long seed) {
        return new GameSettings("", "", difficulty, seed);
    }

    public static GameSettings forNewGame(String difficulty, long seed,
                                          int cellSide, double windowW, double windowH) {
        return new GameSettings("", "", difficulty, seed, cellSide, windowW, windowH);
    }

    public boolean hasUiSizing() {
        return cellSide > 0 && windowW > 0 && windowH > 0;
    }
}
