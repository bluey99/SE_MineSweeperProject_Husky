package service;

public enum MusicTheme {

    // ===== UI / MENU THEMES =====
    NEON_AMBIENT(
            "Neon Ambient (Default)",
            "/sounds/music/aboard-a-aurora-game-menu-pulse-203549.mp3"
    ),

    SYNTHWAVE_80S(
            "80s Synthwave",
            "/sounds/music/80s-synth-pop-retrowavesynthwave-70140bpm-312s-12194.mp3"
    ),

    ROBO_POP(
            "Robo Synthwave",
            "/sounds/music/robo-cop-synthwave-100bpm-117s-12714.mp3"
    ),

    DARK_AMBIENT(
            "Dark Ambient",
            "/sounds/music/deep-slow-ambient-444251.mp3"
    ),

    FANTASY_CHOIR(
            "Fantasy Choir",
            "/sounds/music/fairy-chant-elven-song-epic-music-amp-vocals-1229.mp3"
    ),

    // ===== GAME THEMES (NEW) =====
    GAME_EASY_UNDERSEA(
            "Game – Undersea",
            "/sounds/music/chilling-waves-ambient-chill-out-music-for-relaxation-13880.mp3"
    ),

    GAME_MEDIUM_JUNGLE(
            "Game – Jungle",
            "/sounds/music/jungle-ish-beat-for-video-games-314073.mp3"
    ),

    GAME_HARD_VOLCANO(
            "Game – Volcano",
            "/sounds/music/lava-150307.mp3"
    );

    private final String displayName;
    private final String resourcePath;

    MusicTheme(String displayName, String resourcePath) {
        this.displayName = displayName;
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
