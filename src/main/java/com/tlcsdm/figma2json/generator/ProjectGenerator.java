package com.tlcsdm.figma2json.generator;

import java.nio.file.Path;

/**
 * Interface for generating project files from converted Figma data.
 */
public interface ProjectGenerator {

    /**
     * Gets the generator identifier.
     *
     * @return the generator name
     */
    String getName();

    /**
     * Gets a human-readable description of the generator.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Generates project files from the converted data.
     *
     * @param jsonData   the JSON data to process
     * @param outputPath the output directory path
     * @throws Exception if generation fails
     */
    void generate(String jsonData, Path outputPath) throws Exception;

    /**
     * Validates if the generator can process the given data.
     *
     * @param jsonData the JSON data to validate
     * @return true if the generator can process the data
     */
    boolean canGenerate(String jsonData);
}
