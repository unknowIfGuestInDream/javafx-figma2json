package com.tlcsdm.figma2json.converter;

import com.tlcsdm.figma2json.api.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for JsonConverter.
 * Tests for API result to JSON conversion functionality.
 */
class JsonConverterTest {

    private JsonConverter converter;

    @BeforeEach
    void setUp() {
        converter = new JsonConverter();
    }

    @Test
    @DisplayName("getFormat should return JSON")
    void getFormat_returnsJson() {
        assertEquals("JSON", converter.getFormat());
    }

    @Test
    @DisplayName("getFileExtension should return json")
    void getFileExtension_returnsJsonExtension() {
        assertEquals("json", converter.getFileExtension());
    }

    @Test
    @DisplayName("convert should convert Node to JSON string")
    void convert_node_returnsJsonString() {
        Node node = new Node();
        node.setId("123:456");
        node.setName("Test Frame");
        node.setType("FRAME");
        node.setVisible(true);

        String result = converter.convert(node);

        assertNotNull(result);
        assertTrue(result.contains("\"id\": \"123:456\""));
        assertTrue(result.contains("\"name\": \"Test Frame\""));
        assertTrue(result.contains("\"type\": \"FRAME\""));
        assertTrue(result.contains("\"visible\": true"));
    }

    @Test
    @DisplayName("convert should handle null properties with serializeNulls")
    void convert_nodeWithNullProperties_includesNulls() {
        Node node = new Node();
        node.setId("test-id");
        node.setName(null);

        String result = converter.convert(node);

        assertNotNull(result);
        assertTrue(result.contains("\"id\": \"test-id\""));
        assertTrue(result.contains("\"name\": null"));
    }

    @Test
    @DisplayName("convertRawJson should pretty print JSON")
    void convertRawJson_compactJson_returnsPrettyPrinted() {
        String rawJson = "{\"id\":\"123\",\"name\":\"Test\"}";
        
        String result = converter.convertRawJson(rawJson);

        assertNotNull(result);
        assertTrue(result.contains("\"id\": \"123\""));
        assertTrue(result.contains("\n")); // Should have newlines for pretty print
    }

    @Test
    @DisplayName("convertRawJson should handle nested JSON")
    void convertRawJson_nestedJson_returnsPrettyPrinted() {
        String rawJson = "{\"id\":\"123\",\"children\":[{\"id\":\"456\",\"name\":\"Child\"}]}";
        
        String result = converter.convertRawJson(rawJson);

        assertNotNull(result);
        assertTrue(result.contains("\"children\""));
        assertTrue(result.contains("\"id\": \"456\""));
    }

    @Test
    @DisplayName("convertRawJson should handle arrays")
    void convertRawJson_jsonArray_returnsPrettyPrinted() {
        String rawJson = "[{\"id\":\"1\"},{\"id\":\"2\"}]";
        
        String result = converter.convertRawJson(rawJson);

        assertNotNull(result);
        assertTrue(result.contains("["));
        assertTrue(result.contains("]"));
    }

    @Test
    @DisplayName("convert should produce valid parseable JSON")
    void convert_node_producesValidJson() {
        Node node = new Node();
        node.setId("valid-test");
        node.setName("Valid Node");
        node.setType("TEXT");
        node.setOpacity(0.8);

        String result = converter.convert(node);

        // Should not throw exception when re-parsed
        assertDoesNotThrow(() -> converter.convertRawJson(result));
    }
}
