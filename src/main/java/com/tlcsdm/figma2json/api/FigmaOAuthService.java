package com.tlcsdm.figma2json.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.tlcsdm.figma2json.util.SettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling Figma OAuth 2.0 authentication flow.
 * 
 * OAuth flow:
 * 1. User clicks "Authorize" button
 * 2. App opens browser to Figma authorization URL
 * 3. User grants permission
 * 4. Figma redirects to callback URL with authorization code
 * 5. App exchanges code for access token and refresh token
 * 6. App stores tokens and uses access token for API calls
 * 7. When access token expires, app uses refresh token to get new access token
 */
public class FigmaOAuthService {

    private static final Logger logger = LoggerFactory.getLogger(FigmaOAuthService.class);
    
    private static final String OAUTH_AUTHORIZE_URL = "https://www.figma.com/oauth";
    private static final String OAUTH_TOKEN_URL = "https://www.figma.com/api/oauth/token";
    private static final String DEFAULT_SCOPE = "file_read";
    
    private final HttpClient httpClient;
    private final Gson gson;
    private final SettingsManager settingsManager;
    
    // Token expiration tracking
    private Instant tokenExpirationTime;

    public FigmaOAuthService(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new GsonBuilder().create();
    }

    /**
     * Generates the OAuth authorization URL for the user to visit.
     *
     * @param clientId    the OAuth client ID
     * @param redirectUri the redirect URI (must match the registered URI)
     * @param state       a random state value for CSRF protection
     * @return the authorization URL
     */
    public String getAuthorizationUrl(String clientId, String redirectUri, String state) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Client ID is required");
        }
        
        String effectiveRedirectUri = (redirectUri != null && !redirectUri.isBlank()) 
                ? redirectUri 
                : SettingsManager.getDefaultOAuthRedirectUri();
        
        return OAUTH_AUTHORIZE_URL +
                "?client_id=" + urlEncode(clientId) +
                "&redirect_uri=" + urlEncode(effectiveRedirectUri) +
                "&scope=" + urlEncode(DEFAULT_SCOPE) +
                "&state=" + urlEncode(state) +
                "&response_type=code";
    }

    /**
     * Exchanges an authorization code for access and refresh tokens.
     *
     * @param code         the authorization code received from Figma
     * @param clientId     the OAuth client ID
     * @param clientSecret the OAuth client secret
     * @param redirectUri  the redirect URI (must match the one used in authorization)
     * @return CompletableFuture containing the token response
     */
    public CompletableFuture<OAuthTokenResponse> exchangeCodeForToken(
            String code, String clientId, String clientSecret, String redirectUri) {
        
        if (code == null || code.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Authorization code is required"));
        }
        if (clientId == null || clientId.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Client ID is required"));
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Client secret is required"));
        }
        
        String effectiveRedirectUri = (redirectUri != null && !redirectUri.isBlank()) 
                ? redirectUri 
                : SettingsManager.getDefaultOAuthRedirectUri();
        
        String formData = "client_id=" + urlEncode(clientId) +
                "&client_secret=" + urlEncode(clientSecret) +
                "&redirect_uri=" + urlEncode(effectiveRedirectUri) +
                "&code=" + urlEncode(code) +
                "&grant_type=authorization_code";
        
        logger.debug("Exchanging authorization code for tokens");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OAUTH_TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    logger.debug("Token exchange response status: {}", response.statusCode());
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Token exchange failed with status: " + response.statusCode() +
                                ", body: " + response.body());
                    }
                    OAuthTokenResponse tokenResponse = gson.fromJson(response.body(), OAuthTokenResponse.class);
                    
                    // Store tokens in settings
                    storeTokens(tokenResponse);
                    
                    return tokenResponse;
                });
    }

    /**
     * Refreshes the access token using the stored refresh token.
     *
     * @return CompletableFuture containing the new token response
     */
    public CompletableFuture<OAuthTokenResponse> refreshAccessToken() {
        String clientId = settingsManager.getOAuthClientId();
        String clientSecret = settingsManager.getOAuthClientSecret();
        String refreshToken = settingsManager.getOAuthRefreshToken();
        
        if (clientId == null || clientId.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Client ID is not configured"));
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Client secret is not configured"));
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("No refresh token available"));
        }
        
        String formData = "client_id=" + urlEncode(clientId) +
                "&client_secret=" + urlEncode(clientSecret) +
                "&refresh_token=" + urlEncode(refreshToken) +
                "&grant_type=refresh_token";
        
        logger.debug("Refreshing access token");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OAUTH_TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    logger.debug("Token refresh response status: {}", response.statusCode());
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Token refresh failed with status: " + response.statusCode() +
                                ", body: " + response.body());
                    }
                    OAuthTokenResponse tokenResponse = gson.fromJson(response.body(), OAuthTokenResponse.class);
                    
                    // Store new tokens
                    storeTokens(tokenResponse);
                    
                    return tokenResponse;
                });
    }

    /**
     * Refreshes the access token synchronously using the stored refresh token.
     *
     * @return the new token response
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public OAuthTokenResponse refreshAccessTokenSync() throws IOException, InterruptedException {
        String clientId = settingsManager.getOAuthClientId();
        String clientSecret = settingsManager.getOAuthClientSecret();
        String refreshToken = settingsManager.getOAuthRefreshToken();
        
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Client ID is not configured");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalArgumentException("Client secret is not configured");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("No refresh token available");
        }
        
        String formData = "client_id=" + urlEncode(clientId) +
                "&client_secret=" + urlEncode(clientSecret) +
                "&refresh_token=" + urlEncode(refreshToken) +
                "&grant_type=refresh_token";
        
        logger.debug("Refreshing access token (sync)");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OAUTH_TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.debug("Token refresh response status: {}", response.statusCode());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Token refresh failed with status: " + response.statusCode() +
                    ", body: " + response.body());
        }
        
        OAuthTokenResponse tokenResponse = gson.fromJson(response.body(), OAuthTokenResponse.class);
        
        // Store new tokens
        storeTokens(tokenResponse);
        
        return tokenResponse;
    }

    /**
     * Gets a valid access token, refreshing if necessary.
     *
     * @return the access token
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public String getValidAccessToken() throws IOException, InterruptedException {
        String accessToken = settingsManager.getOAuthAccessToken();
        
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("No access token available. Please authorize first.");
        }
        
        // Check if token is expired or about to expire (within 5 minutes)
        if (isTokenExpiredOrExpiring()) {
            String refreshToken = settingsManager.getOAuthRefreshToken();
            if (refreshToken != null && !refreshToken.isBlank()) {
                logger.info("Access token expired or expiring soon, refreshing...");
                OAuthTokenResponse tokenResponse = refreshAccessTokenSync();
                return tokenResponse.getAccessToken();
            } else {
                logger.warn("Access token expired but no refresh token available");
            }
        }
        
        return accessToken;
    }

    /**
     * Checks if OAuth is properly configured with client ID and secret.
     *
     * @return true if OAuth is configured
     */
    public boolean isOAuthConfigured() {
        String clientId = settingsManager.getOAuthClientId();
        String clientSecret = settingsManager.getOAuthClientSecret();
        return clientId != null && !clientId.isBlank() 
                && clientSecret != null && !clientSecret.isBlank();
    }

    /**
     * Checks if the user is authorized (has valid tokens).
     *
     * @return true if authorized
     */
    public boolean isAuthorized() {
        String accessToken = settingsManager.getOAuthAccessToken();
        String refreshToken = settingsManager.getOAuthRefreshToken();
        // User is authorized if they have either an access token or a refresh token
        return (accessToken != null && !accessToken.isBlank()) 
                || (refreshToken != null && !refreshToken.isBlank());
    }

    /**
     * Clears stored OAuth tokens.
     */
    public void clearTokens() {
        settingsManager.setOAuthAccessToken("");
        settingsManager.setOAuthRefreshToken("");
        tokenExpirationTime = null;
        logger.debug("OAuth tokens cleared");
    }

    /**
     * Gets the default redirect URI.
     *
     * @return the default redirect URI
     */
    public static String getDefaultRedirectUri() {
        return SettingsManager.getDefaultOAuthRedirectUri();
    }

    private void storeTokens(OAuthTokenResponse tokenResponse) {
        if (tokenResponse.getAccessToken() != null) {
            settingsManager.setOAuthAccessToken(tokenResponse.getAccessToken());
            logger.debug("Stored new access token");
        }
        if (tokenResponse.getRefreshToken() != null) {
            settingsManager.setOAuthRefreshToken(tokenResponse.getRefreshToken());
            logger.debug("Stored new refresh token");
        }
        
        // Track token expiration
        if (tokenResponse.getExpiresIn() > 0) {
            tokenExpirationTime = Instant.now().plusSeconds(tokenResponse.getExpiresIn());
            logger.debug("Token expires at: {}", tokenExpirationTime);
        }
    }

    private boolean isTokenExpiredOrExpiring() {
        if (tokenExpirationTime == null) {
            // If we don't know when the token expires, assume it might be expired
            // This is conservative but safe
            return true;
        }
        // Consider token expired if it will expire within 5 minutes
        return Instant.now().isAfter(tokenExpirationTime.minusSeconds(300));
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Response object for OAuth token endpoints.
     */
    public static class OAuthTokenResponse {
        @SerializedName("access_token")
        private String accessToken;
        
        @SerializedName("refresh_token")
        private String refreshToken;
        
        @SerializedName("expires_in")
        private long expiresIn;
        
        @SerializedName("token_type")
        private String tokenType;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }
    }
}
