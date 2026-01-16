package com.tlcsdm.figma2json.converter;

import com.tlcsdm.figma2json.api.Node;

/**
 * Interface for converting Figma nodes to different output formats.
 */
public interface FigmaConverter {

    /**
     * Gets the format identifier.
     *
     * @return the format name (e.g., "json", "xml")
     */
    String getFormat();

    /**
     * Converts a Figma node to the target format.
     *
     * @param node the Figma node to convert
     * @return the converted content as a string
     */
    String convert(Node node);

    /**
     * Converts raw JSON data to the target format.
     *
     * @param rawJson the raw JSON from Figma API
     * @return the converted content as a string
     */
    String convertRawJson(String rawJson);

    /**
     * Gets the file extension for the output format.
     *
     * @return the file extension without the dot
     */
    String getFileExtension();
}
