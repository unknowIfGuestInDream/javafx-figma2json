package com.tlcsdm.figma2json.ui;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import com.tlcsdm.figma2json.util.SettingsManager;
import com.tlcsdm.figma2json.util.SettingsManager.AuthMode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ResourceBundle;

/**
 * Helper class for managing application preferences using PreferencesFX.
 */
public class PreferencesHelper {

    // Constants for authentication mode display names
    private static final String AUTH_MODE_OAUTH = "OAuth";
    private static final String AUTH_MODE_TOKEN = "Token";

    private final SettingsManager settingsManager;
    private final ResourceBundle bundle;

    private final StringProperty accessToken;
    private final ObjectProperty<String> language;
    private final StringProperty figmaApiUrl;
    private final ObservableList<String> languageOptions;

    // OAuth settings
    private final ObjectProperty<String> authMode;
    private final ObservableList<String> authModeOptions;
    private final StringProperty oauthClientId;
    private final StringProperty oauthClientSecret;
    private final StringProperty oauthRedirectUri;
    
    // Cached auth mode for detecting changes
    private AuthMode lastAuthMode;

    private PreferencesFx preferencesFx;

    public PreferencesHelper(SettingsManager settingsManager, ResourceBundle bundle) {
        this.settingsManager = settingsManager;
        this.bundle = bundle;

        // Initialize properties from stored settings
        this.accessToken = new SimpleStringProperty(settingsManager.getAccessToken());
        this.languageOptions = FXCollections.observableArrayList("English", "中文", "日本語");
        this.language = new SimpleObjectProperty<>(getLanguageDisplayName(settingsManager.getLanguage()));
        this.figmaApiUrl = new SimpleStringProperty(settingsManager.getFigmaApiUrl());

        // Initialize OAuth properties - Token first in the list as it's the default
        this.authModeOptions = FXCollections.observableArrayList(AUTH_MODE_TOKEN, AUTH_MODE_OAUTH);
        this.authMode = new SimpleObjectProperty<>(getAuthModeDisplayName(settingsManager.getAuthMode()));
        this.oauthClientId = new SimpleStringProperty(settingsManager.getOAuthClientId());
        this.oauthClientSecret = new SimpleStringProperty(settingsManager.getOAuthClientSecret());
        this.oauthRedirectUri = new SimpleStringProperty(settingsManager.getOAuthRedirectUri());
        
        // Track the current auth mode
        this.lastAuthMode = settingsManager.getAuthMode();

        // Add listeners to save changes
        setupPropertyListeners();
    }

    private void setupPropertyListeners() {
        accessToken.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                settingsManager.setAccessToken(newVal);
            }
        });

        language.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String langCode = getLanguageCode(newVal);
                settingsManager.setLanguage(langCode);
            }
        });

        figmaApiUrl.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                settingsManager.setFigmaApiUrl(newVal);
            }
        });

        // OAuth listeners
        authMode.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                AuthMode mode = getAuthModeFromDisplayName(newVal);
                settingsManager.setAuthMode(mode);
                // Force rebuild of preferences when mode changes
                if (mode != lastAuthMode) {
                    lastAuthMode = mode;
                    preferencesFx = null; // Force rebuild on next show
                }
            }
        });

        oauthClientId.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                settingsManager.setOAuthClientId(newVal);
            }
        });

        oauthClientSecret.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                settingsManager.setOAuthClientSecret(newVal);
            }
        });

        oauthRedirectUri.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                settingsManager.setOAuthRedirectUri(newVal);
            }
        });
    }

    /**
     * Creates and returns the PreferencesFX instance.
     *
     * @return the PreferencesFX instance
     */
    public PreferencesFx getPreferencesFx() {
        if (preferencesFx == null) {
            preferencesFx = createPreferencesFx();
        }
        return preferencesFx;
    }

    private PreferencesFx createPreferencesFx() {
        AuthMode currentMode = getAuthModeFromDisplayName(authMode.get());
        
        // Build settings groups based on current auth mode
        Group authGroup;
        if (currentMode == AuthMode.TOKEN) {
            // Token mode: show auth mode selector and personal access token
            authGroup = Group.of(bundle.getString("preferences.group.authentication"),
                    Setting.of(bundle.getString("preferences.authMode"), authModeOptions, authMode),
                    Setting.of(bundle.getString("preferences.accessToken"), accessToken)
            );
        } else {
            // OAuth mode: show auth mode selector and OAuth settings
            authGroup = Group.of(bundle.getString("preferences.group.authentication"),
                    Setting.of(bundle.getString("preferences.authMode"), authModeOptions, authMode),
                    Setting.of(bundle.getString("preferences.oauthClientId"), oauthClientId),
                    Setting.of(bundle.getString("preferences.oauthClientSecret"), oauthClientSecret),
                    Setting.of(bundle.getString("preferences.oauthRedirectUri"), oauthRedirectUri)
            );
        }
        
        return PreferencesFx.of(PreferencesHelper.class,
                Category.of(bundle.getString("preferences.category.settings"),
                        authGroup,
                        Group.of(bundle.getString("preferences.group.api"),
                                Setting.of(bundle.getString("preferences.figmaApiUrl"), figmaApiUrl)
                        )
                ),
                Category.of(bundle.getString("preferences.category.system"),
                        Group.of(bundle.getString("preferences.group.language"),
                                Setting.of(bundle.getString("preferences.language"), languageOptions, language)
                        )
                )
        ).persistWindowState(false).saveSettings(true).debugHistoryMode(false).instantPersistent(false)
                .buttonsVisibility(true);
    }

    /**
     * Shows the preferences dialog.
     */
    public void showPreferencesDialog() {
        // Always recreate preferences to reflect current auth mode
        preferencesFx = createPreferencesFx();
        preferencesFx.show();
    }

    private String getLanguageDisplayName(String langCode) {
        return switch (langCode) {
            case "zh" -> "中文";
            case "ja" -> "日本語";
            default -> "English";
        };
    }

    private String getLanguageCode(String displayName) {
        return switch (displayName) {
            case "中文" -> "zh";
            case "日本語" -> "ja";
            default -> "en";
        };
    }

    private String getAuthModeDisplayName(AuthMode mode) {
        return switch (mode) {
            case TOKEN -> AUTH_MODE_TOKEN;
            default -> AUTH_MODE_OAUTH;
        };
    }

    private AuthMode getAuthModeFromDisplayName(String displayName) {
        return switch (displayName) {
            case AUTH_MODE_TOKEN -> AuthMode.TOKEN;
            default -> AuthMode.OAUTH;
        };
    }

    /**
     * Gets the current access token.
     *
     * @return the access token
     */
    public String getAccessToken() {
        return accessToken.get();
    }

    /**
     * Gets the access token property.
     *
     * @return the access token property
     */
    public StringProperty accessTokenProperty() {
        return accessToken;
    }

    /**
     * Gets the current Figma API URL.
     *
     * @return the Figma API URL
     */
    public String getFigmaApiUrl() {
        return figmaApiUrl.get();
    }

    /**
     * Gets the Figma API URL property.
     *
     * @return the Figma API URL property
     */
    public StringProperty figmaApiUrlProperty() {
        return figmaApiUrl;
    }

    /**
     * Gets the current language.
     *
     * @return the language display name
     */
    public String getLanguage() {
        return language.get();
    }

    /**
     * Gets the language property.
     *
     * @return the language property
     */
    public ObjectProperty<String> languageProperty() {
        return language;
    }

    /**
     * Checks if the language has changed from the initial value.
     *
     * @param initialLanguage the initial language code
     * @return true if language has changed
     */
    public boolean hasLanguageChanged(String initialLanguage) {
        String currentLangCode = getLanguageCode(language.get());
        return !currentLangCode.equals(initialLanguage);
    }

    /**
     * Gets the current authentication mode.
     *
     * @return the authentication mode
     */
    public AuthMode getAuthMode() {
        return getAuthModeFromDisplayName(authMode.get());
    }

    /**
     * Gets the authentication mode property.
     *
     * @return the authentication mode property
     */
    public ObjectProperty<String> authModeProperty() {
        return authMode;
    }

    /**
     * Gets the OAuth client ID.
     *
     * @return the OAuth client ID
     */
    public String getOAuthClientId() {
        return oauthClientId.get();
    }

    /**
     * Gets the OAuth client ID property.
     *
     * @return the OAuth client ID property
     */
    public StringProperty oauthClientIdProperty() {
        return oauthClientId;
    }

    /**
     * Gets the OAuth client secret.
     *
     * @return the OAuth client secret
     */
    public String getOAuthClientSecret() {
        return oauthClientSecret.get();
    }

    /**
     * Gets the OAuth client secret property.
     *
     * @return the OAuth client secret property
     */
    public StringProperty oauthClientSecretProperty() {
        return oauthClientSecret;
    }

    /**
     * Gets the OAuth redirect URI.
     *
     * @return the OAuth redirect URI
     */
    public String getOAuthRedirectUri() {
        return oauthRedirectUri.get();
    }

    /**
     * Gets the OAuth redirect URI property.
     *
     * @return the OAuth redirect URI property
     */
    public StringProperty oauthRedirectUriProperty() {
        return oauthRedirectUri;
    }

    /**
     * Gets the OAuth access token from settings.
     *
     * @return the OAuth access token
     */
    public String getOAuthAccessToken() {
        return settingsManager.getOAuthAccessToken();
    }

    /**
     * Gets the OAuth refresh token from settings.
     *
     * @return the OAuth refresh token
     */
    public String getOAuthRefreshToken() {
        return settingsManager.getOAuthRefreshToken();
    }

    /**
     * Gets the effective access token based on the authentication mode.
     * If OAuth mode is selected, returns the OAuth access token.
     * If Token mode is selected, returns the personal access token.
     *
     * @return the effective access token for API calls
     */
    public String getEffectiveAccessToken() {
        if (getAuthMode() == AuthMode.OAUTH) {
            return getOAuthAccessToken();
        } else {
            return getAccessToken();
        }
    }

    /**
     * Gets the settings manager.
     *
     * @return the settings manager
     */
    public SettingsManager getSettingsManager() {
        return settingsManager;
    }
}
