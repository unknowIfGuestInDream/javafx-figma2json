package com.tlcsdm.figma2json;

import com.tlcsdm.figma2json.ui.MainViewController;
import com.tlcsdm.figma2json.ui.PreferencesHelper;
import com.tlcsdm.figma2json.util.SettingsManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Main application entry point for Figma to JSON Exporter.
 */
public class Figma2JsonApp extends Application {

    private static ResourceBundle bundle;
    private static Stage primaryStage;
    private static SettingsManager settingsManager;
    private static PreferencesHelper preferencesHelper;
    private static String initialLanguage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        settingsManager = new SettingsManager();
        initialLanguage = settingsManager.getLanguage();
        
        Locale locale = loadLocaleFromPreferences();
        bundle = ResourceBundle.getBundle("i18n.messages", locale);
        
        preferencesHelper = new PreferencesHelper(settingsManager, bundle);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"), bundle);
        Parent mainContent = loader.load();
        
        // Get the controller and pass the preferences helper
        MainViewController controller = loader.getController();
        controller.setPreferencesHelper(preferencesHelper);

        // Create menu bar
        MenuBar menuBar = createMenuBar();

        // Create main layout with menu bar
        BorderPane rootPane = new BorderPane();
        rootPane.setTop(menuBar);
        rootPane.setCenter(mainContent);

        Scene scene = new Scene(rootPane, 900, 750);
        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.setScene(scene);
        
        // Set application icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/icons/app-icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        
        primaryStage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File menu
        Menu fileMenu = new Menu(bundle.getString("menu.file"));
        
        MenuItem preferencesItem = new MenuItem(bundle.getString("menu.file.preferences"));
        preferencesItem.setOnAction(e -> showPreferences());
        
        MenuItem restartItem = new MenuItem(bundle.getString("menu.file.restart"));
        restartItem.setOnAction(e -> restartApplication());
        
        MenuItem exitItem = new MenuItem(bundle.getString("menu.file.exit"));
        exitItem.setOnAction(e -> Platform.exit());
        
        fileMenu.getItems().addAll(preferencesItem, new SeparatorMenuItem(), restartItem, exitItem);

        // Help menu
        Menu helpMenu = new Menu(bundle.getString("menu.help"));
        
        MenuItem aboutItem = new MenuItem(bundle.getString("menu.help.about"));
        aboutItem.setOnAction(e -> showAboutDialog());
        
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }

    private void showPreferences() {
        preferencesHelper.showPreferencesDialog();
        
        // Check if language has changed and show restart notification
        if (preferencesHelper.hasLanguageChanged(initialLanguage)) {
            showRestartNotification();
        }
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("about.title"));
        alert.setHeaderText(bundle.getString("about.name"));
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label(bundle.getString("about.version")),
            new Label(bundle.getString("about.description"))
        );
        alert.getDialogPane().setContent(content);
        
        // Set icon on dialog
        try {
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app-icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        
        alert.showAndWait();
    }

    private void showRestartNotification() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("dialog.languageChanged"));
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString("dialog.restartRequired"));
        alert.showAndWait();
    }

    private void restartApplication() {
        // Close current stage and reload with new locale
        primaryStage.close();
        
        // Reset preferences helper for new instance
        preferencesHelper = null;
        
        Platform.runLater(() -> {
            try {
                // Reload locale from preferences (which may have changed)
                settingsManager = new SettingsManager();
                initialLanguage = settingsManager.getLanguage();
                
                // Create new stage and start fresh
                Stage newStage = new Stage();
                start(newStage);
            } catch (Exception e) {
                e.printStackTrace();
                // If restart fails, exit the application
                Platform.exit();
            }
        });
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
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static SettingsManager getSettingsManager() {
        return settingsManager;
    }
}
