package com.tlcsdm.figma2json.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FigmaApiClient.
 * Tests for API debugging and validation.
 */
class FigmaApiClientTest {

    private FigmaApiClient apiClient;

    @BeforeEach
    void setUp() {
        apiClient = new FigmaApiClient();
    }

    @Test
    @DisplayName("extractFileKey should extract file key from standard Figma URL")
    void extractFileKey_standardUrl_returnsFileKey() {
        String url = "https://www.figma.com/file/abc123XYZ/My-Design-File";
        String result = FigmaApiClient.extractFileKey(url);
        assertEquals("abc123XYZ", result);
    }

    @Test
    @DisplayName("extractFileKey should extract file key from design URL format")
    void extractFileKey_designUrl_returnsFileKey() {
        String url = "https://www.figma.com/design/def456ABC/Another-Design";
        String result = FigmaApiClient.extractFileKey(url);
        assertEquals("def456ABC", result);
    }

    @Test
    @DisplayName("extractFileKey should handle URL with query parameters")
    void extractFileKey_urlWithQueryParams_returnsFileKeyWithoutParams() {
        String url = "https://www.figma.com/file/xyz789/Design?node-id=0:1";
        String result = FigmaApiClient.extractFileKey(url);
        assertEquals("xyz789", result);
    }

    @Test
    @DisplayName("extractFileKey should return null for invalid URL")
    void extractFileKey_invalidUrl_returnsNull() {
        String url = "https://example.com/some-path";
        String result = FigmaApiClient.extractFileKey(url);
        assertNull(result);
    }

    @Test
    @DisplayName("extractFileKey should return null for null input")
    void extractFileKey_nullInput_returnsNull() {
        String result = FigmaApiClient.extractFileKey(null);
        assertNull(result);
    }

    @Test
    @DisplayName("extractFileKey should return null for blank input")
    void extractFileKey_blankInput_returnsNull() {
        String result = FigmaApiClient.extractFileKey("   ");
        assertNull(result);
    }

    @Test
    @DisplayName("setBaseUrl should set custom base URL")
    void setBaseUrl_customUrl_setsBaseUrl() {
        apiClient.setBaseUrl("https://custom.figma.api/v1");
        // No exception means success
        assertNotNull(apiClient);
    }

    @Test
    @DisplayName("setBaseUrl should use default URL for blank input")
    void setBaseUrl_blankInput_usesDefault() {
        apiClient.setBaseUrl("");
        // No exception means success
        assertNotNull(apiClient);
    }

    @Test
    @DisplayName("getGson should return non-null Gson instance")
    void getGson_returnsNonNullInstance() {
        assertNotNull(apiClient.getGson());
    }

    @Test
    @DisplayName("FigmaFile should correctly parse JSON with components as a Map")
    void parseJson_componentsAsMap_parsesCorrectly() {
        String json = """
            {
              "name": "Test File",
              "lastModified": "2024-01-01T12:00:00.000Z",
              "version": "123456789",
              "components": {
                "123:456": {
                  "key": "component_key_1",
                  "name": "Button/Primary",
                  "description": "A primary button"
                },
                "789:012": {
                  "key": "component_key_2",
                  "name": "Button/Secondary",
                  "description": "A secondary button"
                }
              }
            }
            """;

        Gson gson = new GsonBuilder().create();
        FigmaFile file = gson.fromJson(json, FigmaFile.class);

        assertNotNull(file);
        assertEquals("Test File", file.getName());
        assertNotNull(file.getComponents());
        assertEquals(2, file.getComponents().size());
        
        // Verify component with node ID "123:456"
        Component component1 = file.getComponents().get("123:456");
        assertNotNull(component1);
        assertEquals("component_key_1", component1.getKey());
        assertEquals("Button/Primary", component1.getName());
        assertEquals("A primary button", component1.getDescription());
        
        // Verify component with node ID "789:012"
        Component component2 = file.getComponents().get("789:012");
        assertNotNull(component2);
        assertEquals("component_key_2", component2.getKey());
        assertEquals("Button/Secondary", component2.getName());
    }

    @Test
    @DisplayName("FigmaFile should correctly parse JSON with documentationLinks as an array")
    void parseJson_documentationLinksAsArray_parsesCorrectly() {
        String json = """
            {
              "name": "Test File",
              "lastModified": "2024-01-01T12:00:00.000Z",
              "version": "123456789",
              "components": {
                "123:456": {
                  "key": "component_key_1",
                  "name": "Button/Primary",
                  "description": "A primary button",
                  "documentationLinks": [
                    {"url": "https://example.com/docs/button"},
                    {"url": "https://example.com/docs/primary-button"}
                  ]
                }
              }
            }
            """;

        Gson gson = new GsonBuilder().create();
        FigmaFile file = gson.fromJson(json, FigmaFile.class);

        assertNotNull(file);
        assertEquals("Test File", file.getName());
        assertNotNull(file.getComponents());
        assertEquals(1, file.getComponents().size());
        
        // Verify component with node ID "123:456"
        Component component = file.getComponents().get("123:456");
        assertNotNull(component);
        assertEquals("component_key_1", component.getKey());
        assertEquals("Button/Primary", component.getName());
        assertEquals("A primary button", component.getDescription());
        
        // Verify documentationLinks is parsed as a list
        assertNotNull(component.getDocumentationLinks());
        assertEquals(2, component.getDocumentationLinks().size());
    }

    /**
     * Integration test for actual API calls.
     * This test is disabled by default as it requires a valid access token.
     * To run this test:
     * 1. Set your Figma access token
     * 2. Replace the file key with a valid Figma file key
     * 3. Remove the @Disabled annotation
     */
    @Test
    @Disabled("Requires valid Figma access token and file key")
    @DisplayName("Integration test: getFileSync should fetch Figma file")
    void getFileSync_validCredentials_returnsFigmaFile() throws Exception {
        // Set your access token here for testing
        String accessToken = "YOUR_ACCESS_TOKEN_HERE";
        String fileKey = "YOUR_FILE_KEY_HERE";

        apiClient.setAccessToken(accessToken);
        FigmaFile file = apiClient.getFileSync(fileKey);

        assertNotNull(file);
        assertNotNull(file.getName());
        assertNotNull(file.getDocument());
    }
}
