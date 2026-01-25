package view;

import javafx.scene.paint.Color;

public enum Theme {

    EASY_SEA(
        "/backgrounds/bg_easy_sea.jpg",
        Color.web("#0FA9E6"),                 // sceneFill
        Color.rgb(6, 48, 88, 0.35),           // overlay
        "#0EA5E9",                            // accent
        "#22C55E",                            // p1Border
        "#FCA5A5",                            // p2Title

        //  Mascots (transparent PNGs)
        "/mascots/jelly_neutral.png",
        "/mascots/jelly_happy.png",
        "/mascots/jelly_sad.png"
    ),

    MED_FOREST(
        "/backgrounds/bg_med_forest.jpg",
        Color.web("#1B4332"),
        Color.rgb(27, 67, 50, 0.38),
        "#F8FAFC",
        "#4ADE80",
        "#FACC15",

        // 🌿 Add later (for now null)
        "/mascots/neutral_snake.png",
        "/mascots/happy_snake.png",
        "/mascots/sad_snake.png"
    ),

    HARD_LAVA(
        "/backgrounds/bg_hard_lava.jpg",
        Color.web("#2B0B0B"),
        Color.rgb(60, 10, 10, 0.55),
        "#FB7185",
        "#F97316",
        "#FCA5A5",

        // 🌋 Add later (for now null)
        "/mascots/neutral_dragon.png",
        "/mascots/happy_dragon.png",
        "/mascots/sad_dragon.png"
    );

    public final String bgPath;
    public final Color sceneFill;
    public final Color overlay;
    public final String accent;
    public final String p1Border;
    public final String p2Title;

    // 🧸 Mascots (optional per theme)
    public final String mascotNeutral;
    public final String mascotHappy;
    public final String mascotSad;

    Theme(
        String bgPath,
        Color sceneFill,
        Color overlay,
        String accent,
        String p1Border,
        String p2Title,
        String mascotNeutral,
        String mascotHappy,
        String mascotSad
    ) {
        this.bgPath = bgPath;
        this.sceneFill = sceneFill;
        this.overlay = overlay;
        this.accent = accent;
        this.p1Border = p1Border;
        this.p2Title = p2Title;

        this.mascotNeutral = mascotNeutral;
        this.mascotHappy = mascotHappy;
        this.mascotSad = mascotSad;
    }
}
