package com.tlcsdm.figma2json.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Factory for creating and managing project generators.
 */
public class GeneratorFactory {

    private static final Map<String, ProjectGenerator> generators = new HashMap<>();

    static {
        // Register default generators
        registerGenerator(new TouchGfxGenerator());
    }

    /**
     * Registers a new generator.
     *
     * @param generator the generator to register
     */
    public static void registerGenerator(ProjectGenerator generator) {
        generators.put(generator.getName().toLowerCase(), generator);
    }

    /**
     * Gets a generator by name.
     *
     * @param name the generator name (case-insensitive)
     * @return the generator, or null if not found
     */
    public static ProjectGenerator getGenerator(String name) {
        return generators.get(name.toLowerCase());
    }

    /**
     * Gets the default generator (TouchGFX Design).
     *
     * @return the TouchGFX generator
     */
    public static ProjectGenerator getDefaultGenerator() {
        return generators.get("touchgfx design");
    }

    /**
     * Gets all available generator names.
     *
     * @return set of generator names
     */
    public static Set<String> getAvailableGenerators() {
        return generators.keySet();
    }

    /**
     * Gets all registered generators.
     *
     * @return map of generator name to generator instance
     */
    public static Map<String, ProjectGenerator> getAllGenerators() {
        return new HashMap<>(generators);
    }
}
