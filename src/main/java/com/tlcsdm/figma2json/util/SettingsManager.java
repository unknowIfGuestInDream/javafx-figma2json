package com.tlcsdm.figma2json.util;

import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Manages application settings using Java Preferences API.
 */
public class SettingsManager {

    private static final String PREF_ACCESS_TOKEN = "accessToken";
    private static final String PREF_LANGUAGE = "language";
    private static final String PREF_OUTPUT_FORMAT = "outputFormat";
    private static final String PREF_GENERATOR = "generator";
    private static final String PREF_OUTPUT_PATH = "outputPath";
    private static final String PREF_LAST_FIGMA_URL = "lastFigmaUrl";

    private final Preferences prefs;

    public SettingsManager() {
        this.prefs = Preferences.userNodeForPackage(SettingsManager.class);
    }

    /**
     * Gets the stored access token.
     *
     * @return the access token, or empty string if not set
     */
    public String getAccessToken() {
        return prefs.get(PREF_ACCESS_TOKEN, "");
    }

    /**
     * Sets the access token.
     *
     * @param token the access token to store
     */
    public void setAccessToken(String token) {
        prefs.put(PREF_ACCESS_TOKEN, token != null ? token : "");
    }

    /**
     * Gets the stored language.
     *
     * @return the language code (en, zh, ja)
     */
    public String getLanguage() {
        return prefs.get(PREF_LANGUAGE, Locale.getDefault().getLanguage());
    }

    /**
     * Sets the language.
     *
     * @param language the language code (en, zh, ja)
     */
    public void setLanguage(String language) {
        prefs.put(PREF_LANGUAGE, language);
    }

    /**
     * Gets the locale based on stored language.
     *
     * @return the Locale
     */
    public Locale getLocale() {
        return switch (getLanguage()) {
            case "zh" -> Locale.CHINESE;
            case "ja" -> Locale.JAPANESE;
            default -> Locale.ENGLISH;
        };
    }

    /**
     * Gets the output format.
     *
     * @return the output format (default: json)
     */
    public String getOutputFormat() {
        return prefs.get(PREF_OUTPUT_FORMAT, "json");
    }

    /**
     * Sets the output format.
     *
     * @param format the output format
     */
    public void setOutputFormat(String format) {
        prefs.put(PREF_OUTPUT_FORMAT, format);
    }

    /**
     * Gets the generator name.
     *
     * @return the generator name (default: TouchGFX Design)
     */
    public String getGenerator() {
        return prefs.get(PREF_GENERATOR, "TouchGFX Design");
    }

    /**
     * Sets the generator name.
     *
     * @param generator the generator name
     */
    public void setGenerator(String generator) {
        prefs.put(PREF_GENERATOR, generator);
    }

    /**
     * Gets the output path.
     *
     * @return the output path, or empty string if not set
     */
    public String getOutputPath() {
        return prefs.get(PREF_OUTPUT_PATH, "");
    }

    /**
     * Sets the output path.
     *
     * @param path the output path
     */
    public void setOutputPath(String path) {
        prefs.put(PREF_OUTPUT_PATH, path != null ? path : "");
    }

    /**
     * Gets the last used Figma URL.
     *
     * @return the last Figma URL, or empty string if not set
     */
    public String getLastFigmaUrl() {
        return prefs.get(PREF_LAST_FIGMA_URL, "");
    }

    /**
     * Sets the last used Figma URL.
     *
     * @param url the Figma URL
     */
    public void setLastFigmaUrl(String url) {
        prefs.put(PREF_LAST_FIGMA_URL, url != null ? url : "");
    }

    /**
     * Clears all stored settings.
     */
    public void clearAll() {
        try {
            prefs.clear();
        } catch (Exception e) {
            // Ignore
        }
    }
}
