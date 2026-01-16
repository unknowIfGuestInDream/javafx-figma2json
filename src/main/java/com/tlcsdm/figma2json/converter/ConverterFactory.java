package com.tlcsdm.figma2json.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Factory for creating and managing converters.
 */
public class ConverterFactory {

    private static final Map<String, FigmaConverter> converters = new HashMap<>();

    static {
        // Register default converters
        registerConverter(new JsonConverter());
    }

    /**
     * Registers a new converter.
     *
     * @param converter the converter to register
     */
    public static void registerConverter(FigmaConverter converter) {
        converters.put(converter.getFormat().toLowerCase(), converter);
    }

    /**
     * Gets a converter by format name.
     *
     * @param format the format name (case-insensitive)
     * @return the converter, or null if not found
     */
    public static FigmaConverter getConverter(String format) {
        return converters.get(format.toLowerCase());
    }

    /**
     * Gets the default converter (JSON).
     *
     * @return the JSON converter
     */
    public static FigmaConverter getDefaultConverter() {
        return converters.get("json");
    }

    /**
     * Gets all available format names.
     *
     * @return set of format names
     */
    public static Set<String> getAvailableFormats() {
        return converters.keySet();
    }
}
