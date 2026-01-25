package service;

public class VisualSettingsService {

    // Overlay darkness: 0 = transparent, 1 = fully dark
    private static double overlayOpacity = 0.30;

    public static double getOverlayOpacity() {
        return overlayOpacity;
    }

    public static void setOverlayOpacity(double value) {
        overlayOpacity = clamp01(value);
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
