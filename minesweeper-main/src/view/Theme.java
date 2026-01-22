package view;

import javafx.scene.paint.Color;

public enum Theme {

    EASY_SEA(
        "/backgrounds/bg_easy_sea.jpg",
        Color.web("#0FA9E6"),                 // sceneFill
        Color.rgb(6, 48, 88, 0.35),           // overlay
        "#6EE7F9",                            // accent
        "#22C55E",
        "#FCA5A5"
    ),

    MED_FOREST(
    	    "/backgrounds/bg_med_forest.jpg",

    	    Color.web("#1B4332"),

    	    Color.rgb(27, 67, 50, 0.38),

    	    "#34D399",

    	    // 🍃 fresh leaf green (Player 1 border)
    	    "#4ADE80",

    	    // 🌞 warm treasure / sunlight highlight
    	    "#FACC15"
    	),


    HARD_LAVA(
        "/backgrounds/bg_hard_lava.jpg",
        Color.web("#2B0B0B"),                 // sceneFill
        Color.rgb(60, 10, 10, 0.55),
        "#FB7185",
        "#F97316",
        "#FCA5A5"
    );

    public final String bgPath;
    public final Color sceneFill;
    public final Color overlay;
    public final String accent;
    public final String p1Border;
    public final String p2Title;

    Theme(
        String bgPath,
        Color sceneFill,
        Color overlay,
        String accent,
        String p1Border,
        String p2Title
    ) {
        this.bgPath = bgPath;
        this.sceneFill = sceneFill;
        this.overlay = overlay;
        this.accent = accent;
        this.p1Border = p1Border;
        this.p2Title = p2Title;
    }
}
