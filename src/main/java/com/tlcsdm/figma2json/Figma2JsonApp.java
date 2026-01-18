package com.tlcsdm.figma2json;

import com.tlcsdm.figma2json.api.FigmaOAuthService;
import com.tlcsdm.figma2json.ui.MainViewController;
import com.tlcsdm.figma2json.ui.PreferencesHelper;
import com.tlcsdm.figma2json.util.SettingsManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Main application entry point for Figma to JSON Exporter.
 */
public class Figma2JsonApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Figma2JsonApp.class);

    private static ResourceBundle bundle;
    private static Stage primaryStage;
    private static SettingsManager settingsManager;
    private static PreferencesHelper preferencesHelper;
    private static String initialLanguage;
    private static FigmaOAuthService oauthService;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        settingsManager = new SettingsManager();
        oauthService = new FigmaOAuthService(settingsManager);
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

        // Calculate window size based on screen dimensions
        // Use 70% width and 75% height to maintain a comfortable aspect ratio for the UI
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(screenBounds.getWidth() * 0.7, 1200);
        double height = Math.min(screenBounds.getHeight() * 0.75, 900);
        
        Scene scene = new Scene(rootPane, width, height);
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
        
        MenuItem authorizeItem = new MenuItem(bundle.getString("button.authorize"));
        authorizeItem.setOnAction(e -> startOAuthAuthorization());
        
        MenuItem revokeAuthItem = new MenuItem(bundle.getString("button.revokeAuth"));
        revokeAuthItem.setOnAction(e -> revokeOAuthAuthorization());
        
        MenuItem restartItem = new MenuItem(bundle.getString("menu.file.restart"));
        restartItem.setOnAction(e -> restartApplication());
        
        MenuItem exitItem = new MenuItem(bundle.getString("menu.file.exit"));
        exitItem.setOnAction(e -> Platform.exit());
        
        fileMenu.getItems().addAll(preferencesItem, new SeparatorMenuItem(), 
                authorizeItem, revokeAuthItem, new SeparatorMenuItem(), 
                restartItem, exitItem);

        // Help menu
        Menu helpMenu = new Menu(bundle.getString("menu.help"));
        
        MenuItem aboutItem = new MenuItem(bundle.getString("menu.help.about"));
        aboutItem.setOnAction(e -> showAboutDialog());
        
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }

    private void startOAuthAuthorization() {
        // Check if OAuth is configured
        if (!oauthService.isOAuthConfigured()) {
            showError(bundle.getString("error.oauthNotConfigured"));
            return;
        }
        
        String clientId = settingsManager.getOAuthClientId();
        String redirectUri = settingsManager.getOAuthRedirectUri();
        String state = UUID.randomUUID().toString();
        
        String authUrl = oauthService.getAuthorizationUrl(clientId, redirectUri, state);
        
        // Open browser for authorization
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
                logger.info("Opened browser for OAuth authorization");
                
                // Show dialog to input authorization code
                showAuthorizationCodeDialog(state);
            } else {
                // Fallback: show the URL for manual copy
                showAuthUrlDialog(authUrl);
            }
        } catch (Exception e) {
            logger.error("Failed to open browser for OAuth authorization", e);
            showError(bundle.getString("error.oauthFailed") + ": " + e.getMessage());
        }
    }
    
    private void showAuthorizationCodeDialog(String state) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(bundle.getString("dialog.oauthCallback.title"));
        dialog.setHeaderText(bundle.getString("dialog.oauthCallback.header"));
        dialog.setContentText(bundle.getString("dialog.oauthCallback.content"));
        
        // Set icon on dialog
        try {
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app-icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isBlank()) {
            String authCode = result.get().trim();
            exchangeAuthorizationCode(authCode);
        }
    }
    
    private void showAuthUrlDialog(String authUrl) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("dialog.oauthCallback.title"));
        alert.setHeaderText("Open the following URL in your browser:");
        
        TextArea textArea = new TextArea(authUrl);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setMinWidth(500);
        alert.showAndWait();
        
        // After URL dialog, show the authorization code input
        showAuthorizationCodeDialog("");
    }
    
    private void exchangeAuthorizationCode(String authCode) {
        String clientId = settingsManager.getOAuthClientId();
        String clientSecret = settingsManager.getOAuthClientSecret();
        String redirectUri = settingsManager.getOAuthRedirectUri();
        
        oauthService.exchangeCodeForToken(authCode, clientId, clientSecret, redirectUri)
                .thenAccept(tokenResponse -> Platform.runLater(() -> {
                    showSuccess(bundle.getString("success.oauthAuthorized"));
                    logger.info("OAuth authorization successful");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showError(bundle.getString("error.oauthFailed") + ": " + ex.getMessage());
                        logger.error("OAuth authorization failed", ex);
                    });
                    return null;
                });
    }
    
    private void revokeOAuthAuthorization() {
        oauthService.clearTokens();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("dialog.success"));
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString("log.oauthRevoked"));
        alert.showAndWait();
    }

    private void showPreferences() {
        preferencesHelper.showPreferencesDialog();
        
        // Check if language has changed and show restart notification
        if (preferencesHelper.hasLanguageChanged(initialLanguage)) {
            showRestartNotification();
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("dialog.error"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("dialog.success"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
