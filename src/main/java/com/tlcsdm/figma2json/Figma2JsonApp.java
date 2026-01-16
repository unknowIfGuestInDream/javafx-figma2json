package com.tlcsdm.figma2json;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Main application entry point for Figma to JSON Exporter.
 */
public class Figma2JsonApp extends Application {

    private static ResourceBundle bundle;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Locale locale = loadLocaleFromPreferences();
        bundle = ResourceBundle.getBundle("i18n.messages", locale);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"), bundle);
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 700);
        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Locale loadLocaleFromPreferences() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(Figma2JsonApp.class);
        String language = prefs.get("language", Locale.getDefault().getLanguage());
        return switch (language) {
            case "zh" -> Locale.CHINESE;
            case "ja" -> Locale.JAPANESE;
            default -> Locale.ENGLISH;
        };
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static void setLocale(Locale locale) {
        bundle = ResourceBundle.getBundle("i18n.messages", locale);
    }
}
