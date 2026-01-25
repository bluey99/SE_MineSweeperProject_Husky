package service;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundService {

	// 🎵 Single background music player
	private static MediaPlayer backgroundMusic;

	// 🎛 User-selected menu theme (persists across games)
	private static MusicTheme menuTheme = MusicTheme.NEON_AMBIENT;

	// 🎶 What is ACTUALLY playing right now
	private static MusicTheme activeTheme = null;

	// 📍 Where we currently are in the app
	private enum Mode { MENU, GAME }
	private static Mode currentMode = Mode.MENU;




    // 🔊 SFX (AudioClip)
    private static AudioClip hoverSound;
    private static AudioClip clickSound;

    private static boolean musicEnabled = true;
    private static boolean sfxEnabled = true;

    private static double musicVolume = 0.25;
    private static double sfxVolume = 0.6;

    /* =========================================================
       INIT
       ========================================================= */

    public static void initUiSounds() {
        if (hoverSound == null) {
            hoverSound = new AudioClip(
                SoundService.class.getResource(
                    "/sounds/sfx/ui-button-click-5-327756.mp3"
                ).toExternalForm()
            );
        }

        if (clickSound == null) {
            clickSound = new AudioClip(
                SoundService.class.getResource(
                    "/sounds/sfx/click-21156.mp3"
                ).toExternalForm()
            );
        }

        applySfxVolume();
    }

    /* =========================================================
       BACKGROUND MUSIC (MediaPlayer)
       ========================================================= */
   

    
    /**
     * Returns the theme that is currently playing.
     */
    public static MusicTheme getCurrentTheme() {
        return activeTheme;
    }

    /**
     * Called when user selects a menu music theme.
     * Applies immediately ONLY if we are in menu.
     */
    public static void setMenuTheme(MusicTheme theme) {
        if (theme == null) return;

        menuTheme = theme;

        // Only change music now if we're currently in the menu
        if (currentMode == Mode.MENU) {
            playTheme(menuTheme);
        }
    }


    
    /**
     * Plays a theme if it is not already playing.
     * Handles stopping, disposing, and reloading safely.
     */
    private static void playTheme(MusicTheme theme) {
        if (!musicEnabled || theme == null) return;

        // Avoid restarting the same track
        if (theme == activeTheme && backgroundMusic != null) return;

        activeTheme = theme;

        // Stop previous music cleanly
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
            backgroundMusic = null;
        }

        var url = SoundService.class.getResource(theme.getResourcePath());
        if (url == null) {
            System.err.println("❌ MUSIC NOT FOUND: " + theme.getResourcePath());
            return;
        }

        Media media = new Media(url.toExternalForm());
        backgroundMusic = new MediaPlayer(media);
        backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
        backgroundMusic.setVolume(clamp01(musicVolume));
        backgroundMusic.play();
    }


    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;

        if (backgroundMusic == null) return;

        if (enabled) {
            backgroundMusic.play();
        } else {
            backgroundMusic.pause(); // 👈 pause, don’t stop
        }
    }

    public static void setMusicVolume(double volume01) {
        musicVolume = clamp01(volume01);

        if (backgroundMusic != null) {
            backgroundMusic.setVolume(musicVolume); // ✅ LIVE update
        }
    }

    public static boolean isMusicEnabled() {
        return musicEnabled;
    }

    public static double getMusicVolume() {
        return musicVolume;
    }
    
 // =========================================================
 // INTENT-BASED MUSIC CONTROL
 // =========================================================

    /**
     * Call when entering the main menu.
     * Restores the user's chosen menu theme.
     */
    public static void playMenuMusic() {
        currentMode = Mode.MENU;
        playTheme(menuTheme);
    }



    /**
     * Call once when the game starts.
     * Overrides menu music with difficulty-based theme.
     */
    public static void playGameMusicForDifficulty(String difficulty) {
        currentMode = Mode.GAME;

        if (difficulty == null) return;

        switch (difficulty) {
            case "Easy"   -> playTheme(MusicTheme.GAME_EASY_UNDERSEA);
            case "Medium" -> playTheme(MusicTheme.GAME_MEDIUM_JUNGLE);
            case "Hard"   -> playTheme(MusicTheme.GAME_HARD_VOLCANO);
            default       -> playTheme(menuTheme);
        }
    }



    /* =========================================================
       SFX
       ========================================================= */

    public static void setSfxEnabled(boolean enabled) {
        sfxEnabled = enabled;
    }

    public static boolean isSfxEnabled() {
        return sfxEnabled;
    }

    public static double getSfxVolume() {
        return sfxVolume;
    }

    public static void setSfxVolume(double volume01) {
        sfxVolume = clamp01(volume01);
        applySfxVolume();
    }

    private static void applySfxVolume() {
        if (hoverSound != null) hoverSound.setVolume(sfxVolume * 0.6);
        if (clickSound != null) clickSound.setVolume(sfxVolume);
    }

    public static void playHover() {
        if (sfxEnabled && hoverSound != null) hoverSound.play();
    }

    public static void playClick() {
        if (sfxEnabled && clickSound != null) clickSound.play();
    }

    /* =========================================================
       BUTTON AUTO-HOOK
       ========================================================= */

    public static void applyUiSoundsWhenReady(Parent root) {
        if (root == null) return;
        applyUiSounds(root);

        root.sceneProperty().addListener((obs, o, scene) -> {
            if (scene == null) return;
            scene.addPostLayoutPulseListener(() -> applyUiSounds(root));
        });
    }

    private static void applyUiSounds(Parent root) {
        for (Node node : root.lookupAll(".button")) {
            if (!(node instanceof Button btn)) continue;

            if (btn.getProperties().containsKey("uiSoundsApplied")) continue;
            btn.getProperties().put("uiSoundsApplied", true);

            btn.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
                if (!btn.isDisabled()) playHover();
            });

            btn.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (!btn.isDisabled()) playClick();
            });
        }
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
