package view.dialogs;

import controller.Main;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import service.MusicTheme;
import service.SoundService;
import service.VisualSettingsService;
import view.Menu;

public class SettingsDialog extends AbstractDialogTemplate {

    @Override
    protected void configureButtons() {
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        //refresh visuals after closing too
        dialog.setOnHidden(e -> refreshIfMenu());
    }

    @Override
    protected void styleButtons() {
        dialog.getDialogPane().lookupAll(".button").forEach(btn ->
                btn.setStyle(
                        "-fx-background-color: #1e293b;" +
                        "-fx-text-fill: #e5e7eb;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
                )
        );
    }

    @Override
    protected String getTitle() {
        return "Settings";
    }

    @Override
    protected String getMessage() {
        return "Customize preferences";
    }

    //  THIS is the hook from AbstractDialogTemplate
    @Override
    protected Node buildExtraContent() {

        SoundService.initUiSounds();

        /* ================= VISUALS ================= */
        Label visualsHeader = sectionHeader("Visuals");

        Slider brightnessSlider = new Slider(0, 1, 1 - VisualSettingsService.getOverlayOpacity());
        brightnessSlider.setShowTickMarks(true);
        brightnessSlider.setShowTickLabels(true);
        brightnessSlider.setMajorTickUnit(0.10);
        brightnessSlider.setBlockIncrement(0.05);

        Label brightnessValue = new Label();
        brightnessValue.setStyle("-fx-text-fill: #9CA3AF;");
        brightnessValue.textProperty().bind(Bindings.format("%.0f%%", brightnessSlider.valueProperty().multiply(100)));

        brightnessSlider.valueProperty().addListener((obs, oldV, newV) -> {
            double brightness = newV.doubleValue();
            double overlayOpacity = 1 - brightness;

            VisualSettingsService.setOverlayOpacity(overlayOpacity);
            refreshIfMenu();
        });


        GridPane visualsGrid = new GridPane();
        visualsGrid.setHgap(12);
        visualsGrid.setVgap(12);
        visualsGrid.setPadding(new Insets(0, 0, 0, 16));
        visualsGrid.add(styledLabel("Brightness"), 0, 0);
        visualsGrid.add(brightnessSlider, 1, 0);
        visualsGrid.add(brightnessValue, 2, 0);

        /* ================= MUSIC ================= */
        Label musicHeader = sectionHeader("Music");

        CheckBox musicToggle = new CheckBox("Enable Music");
        musicToggle.setSelected(SoundService.isMusicEnabled());
        musicToggle.setStyle("-fx-text-fill: white;");
        musicToggle.selectedProperty().addListener((obs, o, v) -> SoundService.setMusicEnabled(v));

        ComboBox<MusicTheme> musicSelect = new ComboBox<>();
        musicSelect.getItems().addAll(MusicTheme.values());
        musicSelect.setValue(SoundService.getCurrentTheme());
        musicSelect.setOnAction(e ->SoundService.setMenuTheme(musicSelect.getValue()));
        musicSelect.disableProperty().bind(musicToggle.selectedProperty().not());

        Slider musicSlider = createSlider(SoundService.getMusicVolume());
        Label musicValue = createPercentLabel(musicSlider);
        musicSlider.valueProperty().addListener((obs, o, v) -> SoundService.setMusicVolume(v.doubleValue()));
        musicSlider.disableProperty().bind(musicToggle.selectedProperty().not());
        musicValue.disableProperty().bind(musicToggle.selectedProperty().not());

        GridPane musicGrid = new GridPane();
        musicGrid.setHgap(12);
        musicGrid.setVgap(12);
        musicGrid.setPadding(new Insets(0, 0, 0, 16));
        musicGrid.add(styledLabel("Theme"), 0, 0);
        musicGrid.add(musicSelect, 1, 0);
        musicGrid.add(styledLabel("Volume"), 0, 1);
        musicGrid.add(musicSlider, 1, 1);
        musicGrid.add(musicValue, 2, 1);

        /* ================= SFX ================= */
        Label sfxHeader = sectionHeader("Sound Effects");

        CheckBox sfxToggle = new CheckBox("Enable Sound Effects");
        sfxToggle.setSelected(SoundService.isSfxEnabled());
        sfxToggle.setStyle("-fx-text-fill: white;");
        sfxToggle.selectedProperty().addListener((obs, o, v) -> SoundService.setSfxEnabled(v));

        Slider sfxSlider = createSlider(SoundService.getSfxVolume());
        Label sfxValue = createPercentLabel(sfxSlider);
        sfxSlider.valueProperty().addListener((obs, o, v) -> SoundService.setSfxVolume(v.doubleValue()));
        sfxSlider.disableProperty().bind(sfxToggle.selectedProperty().not());
        sfxValue.disableProperty().bind(sfxToggle.selectedProperty().not());

        GridPane sfxGrid = new GridPane();
        sfxGrid.setHgap(12);
        sfxGrid.setVgap(12);
        sfxGrid.setPadding(new Insets(0, 0, 0, 16));
        sfxGrid.add(styledLabel("Volume"), 0, 0);
        sfxGrid.add(sfxSlider, 1, 0);
        sfxGrid.add(sfxValue, 2, 0);

        VBox all = new VBox(
                14,
                visualsHeader, visualsGrid,
                new Separator(),
                musicHeader, musicToggle, musicGrid,
                new Separator(),
                sfxHeader, sfxToggle, sfxGrid
        );

        all.setPadding(new Insets(12));
        return all;
    }

    // refresh only if Menu is the current root
    private void refreshIfMenu() {
        if (Main.getPrimaryStage() == null) return;
        if (Main.getPrimaryStage().getScene() == null) return;

        if (Main.getPrimaryStage().getScene().getRoot() instanceof Menu menu) {
            menu.refreshVisuals();
        }
    }

    /* ---------- helpers ---------- */

    private Slider createSlider(double initial) {
        Slider s = new Slider(0, 1, initial);
        s.setShowTickMarks(true);
        s.setShowTickLabels(true);
        s.setMajorTickUnit(0.25);
        s.setBlockIncrement(0.05);
        return s;
    }

    private Label createPercentLabel(Slider slider) {
        Label lbl = new Label();
        lbl.setStyle("-fx-text-fill: #9CA3AF;");
        lbl.textProperty().bind(
                Bindings.format("%.0f%%", slider.valueProperty().multiply(100))
        );
        return lbl;
    }

    private Label styledLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #E5E7EB; -fx-font-weight: bold;");
        return lbl;
    }

    private Label sectionHeader(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(
                "-fx-text-fill: #E5E7EB;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
        );
        return lbl;
    }
}
