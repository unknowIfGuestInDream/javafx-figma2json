package com.tlcsdm.figma2json.util;

import com.tlcsdm.figma2json.util.SettingsManager.AuthMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SettingsManager OAuth settings.
 */
class SettingsManagerTest {

    private SettingsManager settingsManager;

    @BeforeEach
    void setUp() {
        settingsManager = new SettingsManager();
        // Clear any existing settings before each test
        settingsManager.clearAll();
    }

    @AfterEach
    void tearDown() {
        // Clean up settings after each test
        settingsManager.clearAll();
    }

    @Test
    @DisplayName("default auth mode should be OAuth")
    void getAuthMode_default_returnsOAuth() {
        assertEquals(AuthMode.OAUTH, settingsManager.getAuthMode());
    }

    @Test
    @DisplayName("setAuthMode should set OAuth mode")
    void setAuthMode_oauth_setsOAuthMode() {
        settingsManager.setAuthMode(AuthMode.OAUTH);
        assertEquals(AuthMode.OAUTH, settingsManager.getAuthMode());
    }

    @Test
    @DisplayName("setAuthMode should set Token mode")
    void setAuthMode_token_setsTokenMode() {
        settingsManager.setAuthMode(AuthMode.TOKEN);
        assertEquals(AuthMode.TOKEN, settingsManager.getAuthMode());
    }

    @Test
    @DisplayName("setAuthMode should default to OAuth for null")
    void setAuthMode_null_defaultsToOAuth() {
        settingsManager.setAuthMode(null);
        assertEquals(AuthMode.OAUTH, settingsManager.getAuthMode());
    }

    @Test
    @DisplayName("getOAuthClientId should return empty string by default")
    void getOAuthClientId_default_returnsEmptyString() {
        assertEquals("", settingsManager.getOAuthClientId());
    }

    @Test
    @DisplayName("setOAuthClientId should store and retrieve client ID")
    void setOAuthClientId_validValue_storesValue() {
        String clientId = "test-client-id-123";
        settingsManager.setOAuthClientId(clientId);
        assertEquals(clientId, settingsManager.getOAuthClientId());
    }

    @Test
    @DisplayName("setOAuthClientId should handle null")
    void setOAuthClientId_null_storesEmptyString() {
        settingsManager.setOAuthClientId(null);
        assertEquals("", settingsManager.getOAuthClientId());
    }

    @Test
    @DisplayName("getOAuthClientSecret should return empty string by default")
    void getOAuthClientSecret_default_returnsEmptyString() {
        assertEquals("", settingsManager.getOAuthClientSecret());
    }

    @Test
    @DisplayName("setOAuthClientSecret should store and retrieve client secret")
    void setOAuthClientSecret_validValue_storesValue() {
        String clientSecret = "test-client-secret-456";
        settingsManager.setOAuthClientSecret(clientSecret);
        assertEquals(clientSecret, settingsManager.getOAuthClientSecret());
    }

    @Test
    @DisplayName("setOAuthClientSecret should handle null")
    void setOAuthClientSecret_null_storesEmptyString() {
        settingsManager.setOAuthClientSecret(null);
        assertEquals("", settingsManager.getOAuthClientSecret());
    }

    @Test
    @DisplayName("getOAuthAccessToken should return empty string by default")
    void getOAuthAccessToken_default_returnsEmptyString() {
        assertEquals("", settingsManager.getOAuthAccessToken());
    }

    @Test
    @DisplayName("setOAuthAccessToken should store and retrieve access token")
    void setOAuthAccessToken_validValue_storesValue() {
        String accessToken = "oauth-access-token-789";
        settingsManager.setOAuthAccessToken(accessToken);
        assertEquals(accessToken, settingsManager.getOAuthAccessToken());
    }

    @Test
    @DisplayName("setOAuthAccessToken should handle null")
    void setOAuthAccessToken_null_storesEmptyString() {
        settingsManager.setOAuthAccessToken(null);
        assertEquals("", settingsManager.getOAuthAccessToken());
    }

    @Test
    @DisplayName("getOAuthRefreshToken should return empty string by default")
    void getOAuthRefreshToken_default_returnsEmptyString() {
        assertEquals("", settingsManager.getOAuthRefreshToken());
    }

    @Test
    @DisplayName("setOAuthRefreshToken should store and retrieve refresh token")
    void setOAuthRefreshToken_validValue_storesValue() {
        String refreshToken = "oauth-refresh-token-000";
        settingsManager.setOAuthRefreshToken(refreshToken);
        assertEquals(refreshToken, settingsManager.getOAuthRefreshToken());
    }

    @Test
    @DisplayName("setOAuthRefreshToken should handle null")
    void setOAuthRefreshToken_null_storesEmptyString() {
        settingsManager.setOAuthRefreshToken(null);
        assertEquals("", settingsManager.getOAuthRefreshToken());
    }

    @Test
    @DisplayName("clearAll should clear OAuth settings")
    void clearAll_clearsOAuthSettings() {
        // Set some values first
        settingsManager.setAuthMode(AuthMode.TOKEN);
        settingsManager.setOAuthClientId("client-id");
        settingsManager.setOAuthClientSecret("client-secret");
        settingsManager.setOAuthAccessToken("access-token");
        settingsManager.setOAuthRefreshToken("refresh-token");

        // Clear all
        settingsManager.clearAll();

        // Verify defaults are restored
        assertEquals(AuthMode.OAUTH, settingsManager.getAuthMode());
        assertEquals("", settingsManager.getOAuthClientId());
        assertEquals("", settingsManager.getOAuthClientSecret());
        assertEquals("", settingsManager.getOAuthAccessToken());
        assertEquals("", settingsManager.getOAuthRefreshToken());
    }

    @Test
    @DisplayName("getOAuthRedirectUri should return default value")
    void getOAuthRedirectUri_default_returnsDefault() {
        assertEquals("http://localhost:8888/callback", settingsManager.getOAuthRedirectUri());
    }

    @Test
    @DisplayName("setOAuthRedirectUri should store and retrieve redirect URI")
    void setOAuthRedirectUri_validValue_storesValue() {
        String redirectUri = "http://myapp.example.com/callback";
        settingsManager.setOAuthRedirectUri(redirectUri);
        assertEquals(redirectUri, settingsManager.getOAuthRedirectUri());
    }

    @Test
    @DisplayName("setOAuthRedirectUri should use default for null")
    void setOAuthRedirectUri_null_usesDefault() {
        settingsManager.setOAuthRedirectUri(null);
        assertEquals("http://localhost:8888/callback", settingsManager.getOAuthRedirectUri());
    }

    @Test
    @DisplayName("setOAuthRedirectUri should use default for blank")
    void setOAuthRedirectUri_blank_usesDefault() {
        settingsManager.setOAuthRedirectUri("  ");
        assertEquals("http://localhost:8888/callback", settingsManager.getOAuthRedirectUri());
    }

    @Test
    @DisplayName("getDefaultOAuthRedirectUri should return static default")
    void getDefaultOAuthRedirectUri_returnsStaticDefault() {
        assertEquals("http://localhost:8888/callback", SettingsManager.getDefaultOAuthRedirectUri());
    }
}
