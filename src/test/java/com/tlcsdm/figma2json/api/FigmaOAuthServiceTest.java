package com.tlcsdm.figma2json.api;

import com.tlcsdm.figma2json.util.SettingsManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FigmaOAuthService.
 */
class FigmaOAuthServiceTest {

    private SettingsManager settingsManager;
    private FigmaOAuthService oauthService;

    @BeforeEach
    void setUp() {
        settingsManager = new SettingsManager();
        settingsManager.clearAll();
        oauthService = new FigmaOAuthService(settingsManager);
    }

    @AfterEach
    void tearDown() {
        settingsManager.clearAll();
    }

    @Test
    @DisplayName("getAuthorizationUrl should generate valid URL")
    void getAuthorizationUrl_validClientId_generatesUrl() {
        String clientId = "test-client-id";
        String redirectUri = "http://localhost:8888/callback";
        String state = "random-state";

        String url = oauthService.getAuthorizationUrl(clientId, redirectUri, state);

        assertNotNull(url);
        assertTrue(url.startsWith("https://www.figma.com/oauth"));
        assertTrue(url.contains("client_id=test-client-id"));
        assertTrue(url.contains("redirect_uri="));
        assertTrue(url.contains("state=random-state"));
        assertTrue(url.contains("response_type=code"));
        assertTrue(url.contains("scope=file_content%3Aread"));
    }

    @Test
    @DisplayName("getAuthorizationUrl should use default redirect URI when null")
    void getAuthorizationUrl_nullRedirectUri_usesDefault() {
        String clientId = "test-client-id";
        String state = "random-state";

        String url = oauthService.getAuthorizationUrl(clientId, null, state);

        assertNotNull(url);
        assertTrue(url.contains("redirect_uri=http%3A%2F%2Flocalhost%3A8888%2Fcallback"));
    }

    @Test
    @DisplayName("getAuthorizationUrl should throw exception for null client ID")
    void getAuthorizationUrl_nullClientId_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                oauthService.getAuthorizationUrl(null, "http://localhost:8888/callback", "state"));
    }

    @Test
    @DisplayName("getAuthorizationUrl should throw exception for blank client ID")
    void getAuthorizationUrl_blankClientId_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                oauthService.getAuthorizationUrl("  ", "http://localhost:8888/callback", "state"));
    }

    @Test
    @DisplayName("isOAuthConfigured should return false when not configured")
    void isOAuthConfigured_notConfigured_returnsFalse() {
        assertFalse(oauthService.isOAuthConfigured());
    }

    @Test
    @DisplayName("isOAuthConfigured should return false with only client ID")
    void isOAuthConfigured_onlyClientId_returnsFalse() {
        settingsManager.setOAuthClientId("test-client-id");
        assertFalse(oauthService.isOAuthConfigured());
    }

    @Test
    @DisplayName("isOAuthConfigured should return false with only client secret")
    void isOAuthConfigured_onlyClientSecret_returnsFalse() {
        settingsManager.setOAuthClientSecret("test-client-secret");
        assertFalse(oauthService.isOAuthConfigured());
    }

    @Test
    @DisplayName("isOAuthConfigured should return true when configured")
    void isOAuthConfigured_configured_returnsTrue() {
        settingsManager.setOAuthClientId("test-client-id");
        settingsManager.setOAuthClientSecret("test-client-secret");
        assertTrue(oauthService.isOAuthConfigured());
    }

    @Test
    @DisplayName("isAuthorized should return false when no tokens")
    void isAuthorized_noTokens_returnsFalse() {
        assertFalse(oauthService.isAuthorized());
    }

    @Test
    @DisplayName("isAuthorized should return true with access token")
    void isAuthorized_withAccessToken_returnsTrue() {
        settingsManager.setOAuthAccessToken("test-access-token");
        assertTrue(oauthService.isAuthorized());
    }

    @Test
    @DisplayName("isAuthorized should return true with refresh token")
    void isAuthorized_withRefreshToken_returnsTrue() {
        settingsManager.setOAuthRefreshToken("test-refresh-token");
        assertTrue(oauthService.isAuthorized());
    }

    @Test
    @DisplayName("clearTokens should clear all tokens")
    void clearTokens_clearsAllTokens() {
        settingsManager.setOAuthAccessToken("access-token");
        settingsManager.setOAuthRefreshToken("refresh-token");

        oauthService.clearTokens();

        assertEquals("", settingsManager.getOAuthAccessToken());
        assertEquals("", settingsManager.getOAuthRefreshToken());
        assertFalse(oauthService.isAuthorized());
    }

    @Test
    @DisplayName("getDefaultRedirectUri should return default value")
    void getDefaultRedirectUri_returnsDefaultValue() {
        assertEquals("http://localhost:8888/callback", FigmaOAuthService.getDefaultRedirectUri());
    }

    @Test
    @DisplayName("getValidAccessToken should throw when no access token")
    void getValidAccessToken_noAccessToken_throwsException() {
        assertThrows(IllegalStateException.class, () -> oauthService.getValidAccessToken());
    }

    @Test
    @DisplayName("refreshAccessToken should fail when not configured")
    void refreshAccessToken_notConfigured_fails() {
        var future = oauthService.refreshAccessToken();
        assertNotNull(future);
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    @DisplayName("exchangeCodeForToken should fail with null code")
    void exchangeCodeForToken_nullCode_fails() {
        var future = oauthService.exchangeCodeForToken(null, "client-id", "client-secret", "redirect-uri");
        assertNotNull(future);
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    @DisplayName("exchangeCodeForToken should fail with null client ID")
    void exchangeCodeForToken_nullClientId_fails() {
        var future = oauthService.exchangeCodeForToken("code", null, "client-secret", "redirect-uri");
        assertNotNull(future);
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    @DisplayName("exchangeCodeForToken should fail with null client secret")
    void exchangeCodeForToken_nullClientSecret_fails() {
        var future = oauthService.exchangeCodeForToken("code", "client-id", null, "redirect-uri");
        assertNotNull(future);
        assertTrue(future.isCompletedExceptionally());
    }
}
