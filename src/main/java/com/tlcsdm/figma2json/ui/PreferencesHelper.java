package com.tlcsdm.figma2json.ui;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import com.tlcsdm.figma2json.util.SettingsManager;
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

    private final SettingsManager settingsManager;
    private final ResourceBundle bundle;

    private final StringProperty accessToken;
    private final ObjectProperty<String> language;
    private final StringProperty figmaApiUrl;
    private final ObservableList<String> languageOptions;

    private PreferencesFx preferencesFx;

    public PreferencesHelper(SettingsManager settingsManager, ResourceBundle bundle) {
        this.settingsManager = settingsManager;
        this.bundle = bundle;

        // Initialize properties from stored settings
        this.accessToken = new SimpleStringProperty(settingsManager.getAccessToken());
        this.languageOptions = FXCollections.observableArrayList("English", "中文", "日本語");
        this.language = new SimpleObjectProperty<>(getLanguageDisplayName(settingsManager.getLanguage()));
        this.figmaApiUrl = new SimpleStringProperty(settingsManager.getFigmaApiUrl());

        // Add listeners to save changes
        setupPropertyListeners();
    }

    private void setupPropertyListeners() {
        accessToken.addListener((obs, oldVal, newVal) -> {
            settingsManager.setAccessToken(newVal);
        });

        language.addListener((obs, oldVal, newVal) -> {
            String langCode = getLanguageCode(newVal);
            settingsManager.setLanguage(langCode);
        });

        figmaApiUrl.addListener((obs, oldVal, newVal) -> {
            settingsManager.setFigmaApiUrl(newVal);
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
        return PreferencesFx.of(PreferencesHelper.class,
                Category.of(bundle.getString("preferences.category.general"),
                        Group.of(bundle.getString("preferences.group.authentication"),
                                Setting.of(bundle.getString("preferences.accessToken"), accessToken)
                        ),
                        Group.of(bundle.getString("preferences.group.language"),
                                Setting.of(bundle.getString("preferences.language"), languageOptions, language)
                        ),
                        Group.of(bundle.getString("preferences.group.api"),
                                Setting.of(bundle.getString("preferences.figmaApiUrl"), figmaApiUrl)
                        )
                )
        ).persistWindowState(false).saveSettings(true).debugHistoryMode(false).instantPersistent(true);
    }

    /**
     * Shows the preferences dialog.
     */
    public void showPreferencesDialog() {
        getPreferencesFx().show();
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
}
