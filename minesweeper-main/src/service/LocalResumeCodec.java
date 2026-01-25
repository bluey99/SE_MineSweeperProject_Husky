package service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Properties;

/**
 * Converts LocalGameState <-> String payload.
 * This payload is stored by SysData as an opaque string.
 */
public final class LocalResumeCodec {

    private static final String KEY_VERSION = "version";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_P1 = "p1";
    private static final String KEY_P2 = "p2";
    private static final String KEY_SEED = "seed";
    private static final String KEY_CURRENT_PLAYER = "currentPlayer";
    private static final String KEY_SCORE = "sharedScore";
    private static final String KEY_LIVES = "sharedLives";
    private static final String KEY_TIME = "elapsedTimeSec";

    private static final String KEY_B1_ROWS = "b1.rows";
    private static final String KEY_B1_COLS = "b1.cols";
    private static final String KEY_B2_ROWS = "b2.rows";
    private static final String KEY_B2_COLS = "b2.cols";

    private static final String KEY_B1_OPEN = "b1.open";
    private static final String KEY_B1_FLAG = "b1.flag";
    private static final String KEY_B1_DISC = "b1.discovered";
    private static final String KEY_B1_ACT = "b1.activated";
    private static final String KEY_B1_FS = "b1.flagScored";

    private static final String KEY_B2_OPEN = "b2.open";
    private static final String KEY_B2_FLAG = "b2.flag";
    private static final String KEY_B2_DISC = "b2.discovered";
    private static final String KEY_B2_ACT = "b2.activated";
    private static final String KEY_B2_FS = "b2.flagScored";

    private LocalResumeCodec() {}

    public static String encode(LocalGameState st) {
        if (st == null) return "";

        Properties p = new Properties();
        p.setProperty(KEY_VERSION, String.valueOf(LocalGameState.VERSION));
        p.setProperty(KEY_DIFFICULTY, nullToEmpty(st.difficulty));
        p.setProperty(KEY_P1, nullToEmpty(st.p1Name));
        p.setProperty(KEY_P2, nullToEmpty(st.p2Name));

        p.setProperty(KEY_SEED, String.valueOf(st.seed));
        p.setProperty(KEY_CURRENT_PLAYER, String.valueOf(st.currentPlayer));
        p.setProperty(KEY_SCORE, String.valueOf(st.sharedScore));
        p.setProperty(KEY_LIVES, String.valueOf(st.sharedLives));
        p.setProperty(KEY_TIME, String.valueOf(st.elapsedTimeSec));

        writeBoard(p, st.board1, true);
        writeBoard(p, st.board2, false);

        try {
            StringWriter sw = new StringWriter();
            p.store(sw, "Local resume payload (offline only)");
            return sw.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static Optional<LocalGameState> decode(String payload) {
        if (payload == null) return Optional.empty();
        String trimmed = payload.trim();
        if (trimmed.isEmpty()) return Optional.empty();

        Properties p = new Properties();
        try {
            p.load(new StringReader(trimmed));
        } catch (Exception e) {
            return Optional.empty();
        }

        int version = parseInt(p.getProperty(KEY_VERSION), -1);
        if (version != LocalGameState.VERSION) return Optional.empty();

        LocalGameState st = new LocalGameState();
        st.difficulty = emptyToNull(p.getProperty(KEY_DIFFICULTY));
        st.p1Name = emptyToNull(p.getProperty(KEY_P1));
        st.p2Name = emptyToNull(p.getProperty(KEY_P2));

        st.seed = parseLong(p.getProperty(KEY_SEED), Long.MIN_VALUE);
        st.currentPlayer = parseInt(p.getProperty(KEY_CURRENT_PLAYER), -1);
        st.sharedScore = parseInt(p.getProperty(KEY_SCORE), 0);
        st.sharedLives = parseInt(p.getProperty(KEY_LIVES), 0);
        st.elapsedTimeSec = parseInt(p.getProperty(KEY_TIME), 0);

        if (st.seed == Long.MIN_VALUE) return Optional.empty();
        if (st.currentPlayer != 1 && st.currentPlayer != 2) return Optional.empty();
        if (st.sharedLives < 0 || st.elapsedTimeSec < 0) return Optional.empty();

        try {
            st.board1 = readBoard(p, true);
            st.board2 = readBoard(p, false);
        } catch (Exception e) {
            return Optional.empty();
        }

        if (st.board1.rows <= 0 || st.board1.cols <= 0) return Optional.empty();
        if (st.board2.rows <= 0 || st.board2.cols <= 0) return Optional.empty();

        return Optional.of(st);
    }

    // ---------------- board encode/decode ----------------

    private static void writeBoard(Properties p, LocalGameState.BoardBits b, boolean first) {
        if (b == null) throw new IllegalArgumentException("BoardBits null");

        if (first) {
            p.setProperty(KEY_B1_ROWS, String.valueOf(b.rows));
            p.setProperty(KEY_B1_COLS, String.valueOf(b.cols));
            p.setProperty(KEY_B1_OPEN, LocalGameState.bitsetToBase64(b.open));
            p.setProperty(KEY_B1_FLAG, LocalGameState.bitsetToBase64(b.flag));
            p.setProperty(KEY_B1_DISC, LocalGameState.bitsetToBase64(b.discovered));
            p.setProperty(KEY_B1_ACT, LocalGameState.bitsetToBase64(b.activated));
            p.setProperty(KEY_B1_FS, LocalGameState.bitsetToBase64(b.flagScored));
        } else {
            p.setProperty(KEY_B2_ROWS, String.valueOf(b.rows));
            p.setProperty(KEY_B2_COLS, String.valueOf(b.cols));
            p.setProperty(KEY_B2_OPEN, LocalGameState.bitsetToBase64(b.open));
            p.setProperty(KEY_B2_FLAG, LocalGameState.bitsetToBase64(b.flag));
            p.setProperty(KEY_B2_DISC, LocalGameState.bitsetToBase64(b.discovered));
            p.setProperty(KEY_B2_ACT, LocalGameState.bitsetToBase64(b.activated));
            p.setProperty(KEY_B2_FS, LocalGameState.bitsetToBase64(b.flagScored));
        }
    }

    private static LocalGameState.BoardBits readBoard(Properties p, boolean first) {
        LocalGameState.BoardBits b = new LocalGameState.BoardBits();

        if (first) {
            b.rows = parseInt(p.getProperty(KEY_B1_ROWS), -1);
            b.cols = parseInt(p.getProperty(KEY_B1_COLS), -1);
            if (b.rows <= 0 || b.cols <= 0) throw new IllegalArgumentException("bad b1 dims");

            b.open = LocalGameState.base64ToBitset(p.getProperty(KEY_B1_OPEN));
            b.flag = LocalGameState.base64ToBitset(p.getProperty(KEY_B1_FLAG));
            b.discovered = LocalGameState.base64ToBitset(p.getProperty(KEY_B1_DISC));
            b.activated = LocalGameState.base64ToBitset(p.getProperty(KEY_B1_ACT));
            b.flagScored = LocalGameState.base64ToBitset(p.getProperty(KEY_B1_FS));
        } else {
            b.rows = parseInt(p.getProperty(KEY_B2_ROWS), -1);
            b.cols = parseInt(p.getProperty(KEY_B2_COLS), -1);
            if (b.rows <= 0 || b.cols <= 0) throw new IllegalArgumentException("bad b2 dims");

            b.open = LocalGameState.base64ToBitset(p.getProperty(KEY_B2_OPEN));
            b.flag = LocalGameState.base64ToBitset(p.getProperty(KEY_B2_FLAG));
            b.discovered = LocalGameState.base64ToBitset(p.getProperty(KEY_B2_DISC));
            b.activated = LocalGameState.base64ToBitset(p.getProperty(KEY_B2_ACT));
            b.flagScored = LocalGameState.base64ToBitset(p.getProperty(KEY_B2_FS));
        }

        return b;
    }

    // ---------------- helpers ----------------

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static long parseLong(String s, long def) {
        try { return Long.parseLong(s.trim()); } catch (Exception e) { return def; }
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
