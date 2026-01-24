package view.dialogs;

import javafx.beans.binding.Bindings;
import service.MusicTheme;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import service.SoundService;

public class SettingsDialog extends AbstractDialogTemplate {

    @Override
    protected void configureButtons() {
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
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
        return "Customize audio preferences";
    }

    @Override
    protected void buildContent() {

        SoundService.initUiSounds();

        VBox baseBox = createBaseBox(
                "Settings",
                "Customize audio preferences"
        );

        /* ================= MUSIC SECTION ================= */

        Label musicHeader = sectionHeader("Music");

        CheckBox musicToggle = new CheckBox("Enable Music");
        musicToggle.setSelected(SoundService.isMusicEnabled());
        musicToggle.setStyle("-fx-text-fill: white;");
        musicToggle.selectedProperty().addListener(
                (obs, o, v) -> SoundService.setMusicEnabled(v)
        );

        ComboBox<MusicTheme> musicSelect = new ComboBox<>();
        musicSelect.getItems().addAll(MusicTheme.values());
        musicSelect.setValue(SoundService.getCurrentTheme());
        musicSelect.setOnAction(e ->
                SoundService.setMusicTheme(musicSelect.getValue())
        );
        musicSelect.disableProperty().bind(musicToggle.selectedProperty().not());
        musicSelect.setStyle(
                "-fx-background-color: #1e293b;" +
                "-fx-control-inner-background: #1e293b;" +
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: #9CA3AF;" +
                "-fx-border-color: #334155;" +
                "-fx-border-radius: 6;"
        );
        musicSelect.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(MusicTheme item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: white;");
                }
            }
        });


        Slider musicSlider = createSlider(SoundService.getMusicVolume());
        Label musicValue = createPercentLabel(musicSlider);
        musicSlider.valueProperty().addListener(
                (obs, o, v) -> SoundService.setMusicVolume(v.doubleValue())
        );

        musicSlider.disableProperty().bind(musicToggle.selectedProperty().not());
        musicValue.disableProperty().bind(musicToggle.selectedProperty().not());

        GridPane musicGrid = new GridPane();
        musicGrid.setHgap(12);
        musicGrid.setVgap(12);
        musicGrid.setPadding(new Insets(0, 0, 0, 16)); // 👈 indent

        musicGrid.add(styledLabel("Theme"), 0, 0);
        musicGrid.add(musicSelect, 1, 0);

        musicGrid.add(styledLabel("Volume"), 0, 1);
        musicGrid.add(musicSlider, 1, 1);
        musicGrid.add(musicValue, 2, 1);

        VBox musicSection = new VBox(10, musicToggle, musicGrid);

        /* ================= SFX SECTION ================= */

        Label sfxHeader = sectionHeader("Sound Effects");

        CheckBox sfxToggle = new CheckBox("Enable Sound Effects");
        sfxToggle.setSelected(SoundService.isSfxEnabled());
        sfxToggle.setStyle("-fx-text-fill: white;");
        sfxToggle.selectedProperty().addListener(
                (obs, o, v) -> SoundService.setSfxEnabled(v)
        );

        Slider sfxSlider = createSlider(SoundService.getSfxVolume());
        Label sfxValue = createPercentLabel(sfxSlider);
        sfxSlider.valueProperty().addListener(
                (obs, o, v) -> SoundService.setSfxVolume(v.doubleValue())
        );

        sfxSlider.disableProperty().bind(sfxToggle.selectedProperty().not());
        sfxValue.disableProperty().bind(sfxToggle.selectedProperty().not());

        GridPane sfxGrid = new GridPane();
        sfxGrid.setHgap(12);
        sfxGrid.setVgap(12);
        sfxGrid.setPadding(new Insets(0, 0, 0, 16)); // 👈 indent

        sfxGrid.add(styledLabel("Volume"), 0, 0);
        sfxGrid.add(sfxSlider, 1, 0);
        sfxGrid.add(sfxValue, 2, 0);

        VBox sfxSection = new VBox(10, sfxToggle, sfxGrid);

        /* ================= FINAL LAYOUT ================= */

        VBox content = new VBox(
                18,
                musicHeader,
                musicSection,
                new Separator(),
                sfxHeader,
                sfxSection
        );

        content.setPadding(new Insets(12));

        baseBox.getChildren().add(content);
        dialog.getDialogPane().setContent(baseBox);
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
