package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import view.dialogs.SettingsDialog;
import view.dialogs.HelpDialog;


public class TopBarFactory {

    public static HBox createTopBar() {
    	Button helpBtn = new Button("?");
    	styleSettingsButton(helpBtn);
    	helpBtn.setOnAction(e -> new HelpDialog().show());

    	Button settingsBtn = new Button("⚙");
    	styleSettingsButton(settingsBtn);
    	settingsBtn.setOnAction(e -> new SettingsDialog().show());

    	Region spacer = new Region();
    	HBox.setHgrow(spacer, Priority.ALWAYS);

    	HBox bar = new HBox(10, spacer, helpBtn, settingsBtn);

        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(18, 24, 18, 24));

        return bar;
    }

    private static void styleSettingsButton(Button btn) {
        btn.setPrefSize(42, 42);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        btn.setStyle(baseStyle());

        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle()));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle()));
    }

    private static String baseStyle() {
        return """
            -fx-background-color: rgba(15,23,42,0.85);
            -fx-text-fill: white;
            -fx-background-radius: 999;
            -fx-cursor: hand;
        """;
    }

    private static String hoverStyle() {
        return """
            -fx-background-color: rgba(34,197,94,0.6);
            -fx-text-fill: white;
            -fx-background-radius: 999;
            -fx-cursor: hand;
        """;
    }
}