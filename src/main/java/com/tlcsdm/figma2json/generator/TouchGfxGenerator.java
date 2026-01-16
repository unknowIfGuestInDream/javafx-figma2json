package com.tlcsdm.figma2json.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generator for TouchGFX Design project files.
 * This is the default generator that creates a TouchGFX-compatible project structure.
 */
public class TouchGfxGenerator implements ProjectGenerator {

    private final Gson gson;

    public TouchGfxGenerator() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Override
    public String getName() {
        return "TouchGFX Design";
    }

    @Override
    public String getDescription() {
        return "Generates TouchGFX Design project structure from Figma design data";
    }

    @Override
    public void generate(String jsonData, Path outputPath) throws Exception {
        // Create project directory structure
        Path assetsDir = outputPath.resolve("assets");
        Path imagesDir = assetsDir.resolve("images");
        Path fontsDir = assetsDir.resolve("fonts");
        Path configDir = outputPath.resolve("config");
        Path guiDir = outputPath.resolve("gui");
        Path screensDir = guiDir.resolve("screens");

        Files.createDirectories(imagesDir);
        Files.createDirectories(fontsDir);
        Files.createDirectories(configDir);
        Files.createDirectories(screensDir);

        // Generate project configuration
        generateProjectConfig(jsonData, configDir);

        // Generate screen files
        generateScreenFiles(jsonData, screensDir);

        // Generate README
        generateReadme(outputPath);
    }

    private void generateProjectConfig(String jsonData, Path configDir) throws IOException {
        JsonObject config = new JsonObject();
        config.addProperty("version", "1.0.0");
        config.addProperty("generator", "figma2json");
        config.addProperty("type", "touchgfx");

        Path configFile = configDir.resolve("project.json");
        Files.writeString(configFile, gson.toJson(config));
    }

    private void generateScreenFiles(String jsonData, Path screensDir) throws IOException {
        // Parse the Figma data and extract screen information
        JsonObject figmaData = JsonParser.parseString(jsonData).getAsJsonObject();

        // Save the raw data as screen definition
        Path screenFile = screensDir.resolve("main_screen.json");
        Files.writeString(screenFile, gson.toJson(figmaData));
    }

    private void generateReadme(Path outputPath) throws IOException {
        String readme = """
                # TouchGFX Design Project
                
                This project was generated from Figma design data using figma2json.
                
                ## Structure
                
                - `assets/` - Project assets
                  - `images/` - Image assets
                  - `fonts/` - Font files
                - `config/` - Project configuration
                - `gui/` - GUI components
                  - `screens/` - Screen definitions
                
                ## Usage
                
                Import this project structure into TouchGFX Designer or use the screen 
                definitions directly in your embedded GUI application.
                """;

        Path readmeFile = outputPath.resolve("README.md");
        Files.writeString(readmeFile, readme);
    }

    @Override
    public boolean canGenerate(String jsonData) {
        try {
            JsonParser.parseString(jsonData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
