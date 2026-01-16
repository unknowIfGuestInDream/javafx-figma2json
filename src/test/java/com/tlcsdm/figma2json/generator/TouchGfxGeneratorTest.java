package com.tlcsdm.figma2json.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for TouchGfxGenerator.
 * Tests for JSON to TouchGFX Design conversion functionality.
 */
class TouchGfxGeneratorTest {

    private TouchGfxGenerator generator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        generator = new TouchGfxGenerator();
    }

    @Test
    @DisplayName("getName should return TouchGFX Design")
    void getName_returnsTouchGfxDesign() {
        assertEquals("TouchGFX Design", generator.getName());
    }

    @Test
    @DisplayName("getDescription should return non-empty description")
    void getDescription_returnsNonEmptyDescription() {
        String description = generator.getDescription();
        assertNotNull(description);
        assertFalse(description.isBlank());
        assertTrue(description.contains("TouchGFX"));
    }

    @Test
    @DisplayName("canGenerate should return true for valid JSON")
    void canGenerate_validJson_returnsTrue() {
        String validJson = "{\"id\":\"123\",\"name\":\"Test\"}";
        assertTrue(generator.canGenerate(validJson));
    }

    @Test
    @DisplayName("canGenerate should return false for invalid JSON")
    void canGenerate_invalidJson_returnsFalse() {
        String invalidJson = "not a json string";
        assertFalse(generator.canGenerate(invalidJson));
    }

    @Test
    @DisplayName("canGenerate should return true for complex JSON")
    void canGenerate_complexJson_returnsTrue() {
        String complexJson = """
                {
                    "document": {
                        "id": "0:0",
                        "name": "Document",
                        "type": "DOCUMENT",
                        "children": [
                            {
                                "id": "0:1",
                                "name": "Page 1",
                                "type": "CANVAS"
                            }
                        ]
                    }
                }
                """;
        assertTrue(generator.canGenerate(complexJson));
    }

    @Test
    @DisplayName("generate should create project directory structure")
    void generate_validJson_createsDirectoryStructure() throws Exception {
        String jsonData = "{\"id\":\"123\",\"name\":\"Test Project\"}";
        Path outputPath = tempDir.resolve("test-project");
        Files.createDirectories(outputPath);

        generator.generate(jsonData, outputPath);

        // Check directory structure
        assertTrue(Files.exists(outputPath.resolve("assets")));
        assertTrue(Files.exists(outputPath.resolve("assets/images")));
        assertTrue(Files.exists(outputPath.resolve("assets/fonts")));
        assertTrue(Files.exists(outputPath.resolve("config")));
        assertTrue(Files.exists(outputPath.resolve("gui")));
        assertTrue(Files.exists(outputPath.resolve("gui/screens")));
    }

    @Test
    @DisplayName("generate should create project.json config file")
    void generate_validJson_createsProjectConfig() throws Exception {
        String jsonData = "{\"id\":\"123\",\"name\":\"Test Project\"}";
        Path outputPath = tempDir.resolve("config-test");
        Files.createDirectories(outputPath);

        generator.generate(jsonData, outputPath);

        Path configFile = outputPath.resolve("config/project.json");
        assertTrue(Files.exists(configFile));

        String configContent = Files.readString(configFile);
        assertTrue(configContent.contains("\"version\": \"1.0.0\""));
        assertTrue(configContent.contains("\"generator\": \"figma2json\""));
        assertTrue(configContent.contains("\"type\": \"touchgfx\""));
    }

    @Test
    @DisplayName("generate should create main_screen.json in screens directory")
    void generate_validJson_createsScreenFile() throws Exception {
        String jsonData = "{\"id\":\"test-screen\",\"name\":\"Main Screen\"}";
        Path outputPath = tempDir.resolve("screen-test");
        Files.createDirectories(outputPath);

        generator.generate(jsonData, outputPath);

        Path screenFile = outputPath.resolve("gui/screens/main_screen.json");
        assertTrue(Files.exists(screenFile));

        String screenContent = Files.readString(screenFile);
        assertTrue(screenContent.contains("\"id\": \"test-screen\""));
        assertTrue(screenContent.contains("\"name\": \"Main Screen\""));
    }

    @Test
    @DisplayName("generate should create README.md")
    void generate_validJson_createsReadme() throws Exception {
        String jsonData = "{\"id\":\"123\",\"name\":\"Test\"}";
        Path outputPath = tempDir.resolve("readme-test");
        Files.createDirectories(outputPath);

        generator.generate(jsonData, outputPath);

        Path readmeFile = outputPath.resolve("README.md");
        assertTrue(Files.exists(readmeFile));

        String readmeContent = Files.readString(readmeFile);
        assertTrue(readmeContent.contains("# TouchGFX Design Project"));
        assertTrue(readmeContent.contains("figma2json"));
        assertTrue(readmeContent.contains("## Structure"));
        assertTrue(readmeContent.contains("## Usage"));
    }

    @Test
    @DisplayName("generate should handle nested JSON structure")
    void generate_nestedJson_processesCorrectly() throws Exception {
        String jsonData = """
                {
                    "document": {
                        "id": "0:0",
                        "name": "Document",
                        "children": [
                            {"id": "0:1", "name": "Page 1"},
                            {"id": "0:2", "name": "Page 2"}
                        ]
                    }
                }
                """;
        Path outputPath = tempDir.resolve("nested-test");
        Files.createDirectories(outputPath);

        assertDoesNotThrow(() -> generator.generate(jsonData, outputPath));
        assertTrue(Files.exists(outputPath.resolve("gui/screens/main_screen.json")));
    }
}
