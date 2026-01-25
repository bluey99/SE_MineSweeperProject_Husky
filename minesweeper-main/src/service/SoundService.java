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

    private static double musicVolume = 0.50;
    private static double sfxVolume = 0.6;
    // 🎮 GAME SFX
    private static AudioClip revealNormalSound;
    private static AudioClip revealMineSound;
    private static AudioClip revealSpecialSound;

    private static AudioClip goodSurpriseSound;
    private static AudioClip badSurpriseSound;

    private static AudioClip correctAnswerSound;
    private static AudioClip wrongAnswerSound;
    private static AudioClip flagSound;
    // 🎮 END GAME SFX
    private static AudioClip victorySound;
    private static AudioClip failSound;



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

    public static void initGameSounds() {

        if (revealNormalSound == null) {
            revealNormalSound = new AudioClip(
                SoundService.class.getResource(
                    "/sounds/sfx/soft-subtle-ui-pop-sfx-348820.mp3"
                ).toExternalForm()
            );
        }

        if (revealMineSound == null) {
            revealMineSound = new AudioClip(
                SoundService.class.getResource(
                    "/sounds/sfx/animated-cartoon-explosion-impact-352744.mp3"
                ).toExternalForm()
            );
        }

        if (revealSpecialSound == null) {
            revealSpecialSound = new AudioClip(
                SoundService.class.getResource(
                    "/sounds/sfx/specialCellReveal.mp3"
                ).toExternalForm()
            );
        }

        if (goodSurpriseSound == null) {
            goodSurpriseSound = new AudioClip(
                SoundService.class.getResource(
                    "/sounds/sfx/goodSurprise.mp3"
                ).toExternalForm()
            );
        }

        if (badSurpriseSound == null) {
            badSurpriseSound = new AudioClip(
                SoundService.class.getResource(
                    "/sounds/sfx/badSurprise.mp3"
                ).toExternalForm()
            );
            badSurpriseSound.play(0.0);
        }

        if (correctAnswerSound == null) {
            correctAnswerSound = new AudioClip(
                SoundService.class.getResource(
                    "/sounds/sfx/correctAnswer.mp3"
                ).toExternalForm()
            );
        }

        if (wrongAnswerSound == null) {
            wrongAnswerSound = new AudioClip(
                SoundService.class.getResource(
                    "/sounds/sfx/wrongAnswer.mp3"
                ).toExternalForm()
            );
        }
        if (flagSound == null) {
            flagSound = new AudioClip(
                SoundService.class.getResource(
                    "/sounds/sfx/flagSfx.mp3"
                ).toExternalForm()
            );
        }
        if (victorySound == null) {
            victorySound = new AudioClip(
                SoundService.class.getResource(
                    "/sounds/sfx/victorySound.mp3"
                ).toExternalForm()
            );
        }

        if (failSound == null) {
            failSound = new AudioClip(
                SoundService.class.getResource(
                    "/sounds/sfx/failSound.mp3"
                ).toExternalForm()
            );
        }



        applyGameSfxVolume();
    }
    private static void applyGameSfxVolume() {
        double v = sfxVolume;

        if (revealNormalSound != null) revealNormalSound.setVolume(v * 0.9);
        if (revealSpecialSound != null) revealSpecialSound.setVolume(v);
        if (revealMineSound != null) revealMineSound.setVolume(v * 1.2);

        if (goodSurpriseSound != null) goodSurpriseSound.setVolume(v);
        if (badSurpriseSound != null) badSurpriseSound.setVolume(v);

        if (correctAnswerSound != null) correctAnswerSound.setVolume(v);
        if (wrongAnswerSound != null) wrongAnswerSound.setVolume(1.0);
        if (flagSound != null) flagSound.setVolume(sfxVolume * 0.75);
        if (victorySound != null) victorySound.setVolume(sfxVolume);
        if (failSound != null) failSound.setVolume(sfxVolume);



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

        // apply volume BEFORE playback to avoid loud start
        backgroundMusic.setVolume(musicLoudness(musicVolume));

        if (musicEnabled) {
            backgroundMusic.play();
        }

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
        	backgroundMusic.setVolume(musicLoudness(musicVolume));

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
    
    public static void playRevealNormal() {
        if (sfxEnabled && revealNormalSound != null) revealNormalSound.play();
    }

    public static void playRevealMine() {
        if (sfxEnabled && revealMineSound != null) revealMineSound.play();
    }

    public static void playRevealSpecial() {
        if (sfxEnabled && revealSpecialSound != null) revealSpecialSound.play();
    }

    public static void playGoodSurprise() {
        if (sfxEnabled && goodSurpriseSound != null) goodSurpriseSound.play();
    }

    public static void playBadSurprise() {
        if (sfxEnabled && badSurpriseSound != null) badSurpriseSound.play();
    }

    public static void playCorrectAnswer() {
        if (sfxEnabled && correctAnswerSound != null) correctAnswerSound.play();
    }

    public static void playWrongAnswer() {
        if (sfxEnabled && wrongAnswerSound != null) wrongAnswerSound.play();
    }
    public static void playFlagPlaced() {
        if (sfxEnabled && flagSound != null) flagSound.play(); 
    }
    public static void playVictory() {
        if (sfxEnabled && victorySound != null) victorySound.play();
    }

    public static void playFail() {
        if (sfxEnabled && failSound != null) failSound.play();
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
        applyGameSfxVolume();

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
    
 //  perceptual curve for music loudness
    private static double musicLoudness(double linearVolume) {
        // exponent >1 makes mid values quieter without affecting max
        return Math.pow(clamp01(linearVolume), 1.8);
    }

 // re-apply all current audio settings safely
    public static void applyCurrentSettings() {

        // MUSIC
        if (backgroundMusic != null) {
        	backgroundMusic.setVolume(musicLoudness(musicVolume));


            if (musicEnabled) {
                backgroundMusic.play();
            } else {
                backgroundMusic.pause();
            }
        }

        // SFX
        applySfxVolume();
        applyGameSfxVolume();
    }

}
