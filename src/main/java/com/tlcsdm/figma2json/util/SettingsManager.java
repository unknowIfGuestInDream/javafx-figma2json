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
    private static final String PREF_FIGMA_API_URL = "figmaApiUrl";
    private static final String DEFAULT_FIGMA_API_URL = "https://api.figma.com/v1";

    // OAuth settings
    private static final String PREF_AUTH_MODE = "authMode";
    private static final String PREF_OAUTH_CLIENT_ID = "oauthClientId";
    private static final String PREF_OAUTH_CLIENT_SECRET = "oauthClientSecret";
    private static final String PREF_OAUTH_ACCESS_TOKEN = "oauthAccessToken";
    private static final String PREF_OAUTH_REFRESH_TOKEN = "oauthRefreshToken";
    private static final String PREF_OAUTH_REDIRECT_URI = "oauthRedirectUri";
    private static final String DEFAULT_OAUTH_REDIRECT_URI = "http://localhost:8888/callback";

    /**
     * Authentication mode: OAuth or Token.
     */
    public enum AuthMode {
        OAUTH, TOKEN
    }

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
     * Gets the Figma API URL.
     *
     * @return the Figma API URL, or default if not set
     */
    public String getFigmaApiUrl() {
        return prefs.get(PREF_FIGMA_API_URL, DEFAULT_FIGMA_API_URL);
    }

    /**
     * Sets the Figma API URL.
     *
     * @param url the Figma API URL
     */
    public void setFigmaApiUrl(String url) {
        prefs.put(PREF_FIGMA_API_URL, url != null && !url.isBlank() ? url : DEFAULT_FIGMA_API_URL);
    }

    /**
     * Gets the default Figma API URL.
     *
     * @return the default Figma API URL
     */
    public static String getDefaultFigmaApiUrl() {
        return DEFAULT_FIGMA_API_URL;
    }

    /**
     * Gets the authentication mode.
     *
     * @return the authentication mode (default: TOKEN)
     */
    public AuthMode getAuthMode() {
        String mode = prefs.get(PREF_AUTH_MODE, AuthMode.TOKEN.name());
        try {
            return AuthMode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            return AuthMode.TOKEN;
        }
    }

    /**
     * Sets the authentication mode.
     *
     * @param mode the authentication mode
     */
    public void setAuthMode(AuthMode mode) {
        prefs.put(PREF_AUTH_MODE, mode != null ? mode.name() : AuthMode.TOKEN.name());
    }

    /**
     * Gets the OAuth client ID.
     *
     * @return the OAuth client ID, or empty string if not set
     */
    public String getOAuthClientId() {
        return prefs.get(PREF_OAUTH_CLIENT_ID, "");
    }

    /**
     * Sets the OAuth client ID.
     *
     * @param clientId the OAuth client ID
     */
    public void setOAuthClientId(String clientId) {
        prefs.put(PREF_OAUTH_CLIENT_ID, clientId != null ? clientId : "");
    }

    /**
     * Gets the OAuth client secret.
     *
     * @return the OAuth client secret, or empty string if not set
     */
    public String getOAuthClientSecret() {
        return prefs.get(PREF_OAUTH_CLIENT_SECRET, "");
    }

    /**
     * Sets the OAuth client secret.
     *
     * @param clientSecret the OAuth client secret
     */
    public void setOAuthClientSecret(String clientSecret) {
        prefs.put(PREF_OAUTH_CLIENT_SECRET, clientSecret != null ? clientSecret : "");
    }

    /**
     * Gets the OAuth access token.
     *
     * @return the OAuth access token, or empty string if not set
     */
    public String getOAuthAccessToken() {
        return prefs.get(PREF_OAUTH_ACCESS_TOKEN, "");
    }

    /**
     * Sets the OAuth access token.
     *
     * @param accessToken the OAuth access token
     */
    public void setOAuthAccessToken(String accessToken) {
        prefs.put(PREF_OAUTH_ACCESS_TOKEN, accessToken != null ? accessToken : "");
    }

    /**
     * Gets the OAuth refresh token.
     *
     * @return the OAuth refresh token, or empty string if not set
     */
    public String getOAuthRefreshToken() {
        return prefs.get(PREF_OAUTH_REFRESH_TOKEN, "");
    }

    /**
     * Sets the OAuth refresh token.
     *
     * @param refreshToken the OAuth refresh token
     */
    public void setOAuthRefreshToken(String refreshToken) {
        prefs.put(PREF_OAUTH_REFRESH_TOKEN, refreshToken != null ? refreshToken : "");
    }

    /**
     * Gets the OAuth redirect URI.
     *
     * @return the OAuth redirect URI, or default if not set
     */
    public String getOAuthRedirectUri() {
        String value = prefs.get(PREF_OAUTH_REDIRECT_URI, "");
        // If empty, return and save default
        if (value == null || value.isBlank()) {
            value = DEFAULT_OAUTH_REDIRECT_URI;
            prefs.put(PREF_OAUTH_REDIRECT_URI, value);
        }
        return value;
    }

    /**
     * Sets the OAuth redirect URI.
     *
     * @param redirectUri the OAuth redirect URI
     */
    public void setOAuthRedirectUri(String redirectUri) {
        prefs.put(PREF_OAUTH_REDIRECT_URI, redirectUri != null && !redirectUri.isBlank() 
                ? redirectUri : DEFAULT_OAUTH_REDIRECT_URI);
    }

    /**
     * Gets the default OAuth redirect URI.
     *
     * @return the default OAuth redirect URI
     */
    public static String getDefaultOAuthRedirectUri() {
        return DEFAULT_OAUTH_REDIRECT_URI;
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
