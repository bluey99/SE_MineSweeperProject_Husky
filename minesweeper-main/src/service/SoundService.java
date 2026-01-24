package service;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundService {

    // 🎵 MUSIC (MediaPlayer)
    private static MediaPlayer backgroundMusic;
    private static MusicTheme currentTheme = MusicTheme.NEON_AMBIENT;



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

    public static void playGameBackgroundMusic() {
        if (!musicEnabled) return;

        if (backgroundMusic == null) {
            loadAndPlayTheme(currentTheme);
        } else {
            backgroundMusic.play();
        }
    }
    
    public static MusicTheme getCurrentTheme() {
        return currentTheme;
    }

    
    public static void setMusicTheme(MusicTheme theme) {
        if (theme == null || theme == currentTheme) return;

        currentTheme = theme;

        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
            backgroundMusic = null;
        }

        if (musicEnabled) {
            playGameBackgroundMusic();
        }
    }

    
    private static void loadAndPlayTheme(MusicTheme theme) {
        Media media = new Media(
            SoundService.class
                .getResource(theme.getResourcePath())
                .toExternalForm()
        );

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
