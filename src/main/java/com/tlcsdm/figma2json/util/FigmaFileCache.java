package com.tlcsdm.figma2json.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tlcsdm.figma2json.api.FigmaFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages caching of Figma file data to avoid excessive API calls.
 * Caches are stored as JSON files in the user's home directory.
 */
public class FigmaFileCache {

    private static final Logger logger = LoggerFactory.getLogger(FigmaFileCache.class);
    private static final String CACHE_DIR_NAME = ".figma2json-cache";
    private static final String CACHE_FILE_EXTENSION = ".json";
    
    private final Gson gson;
    private final Path cacheDirectory;

    public FigmaFileCache() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        
        // Create cache directory in user's home directory
        String userHome = System.getProperty("user.home");
        this.cacheDirectory = Paths.get(userHome, CACHE_DIR_NAME);
        
        try {
            if (!Files.exists(cacheDirectory)) {
                Files.createDirectories(cacheDirectory);
                logger.info("Created cache directory: {}", cacheDirectory);
            }
        } catch (IOException e) {
            logger.error("Failed to create cache directory", e);
        }
    }

    /**
     * Saves a FigmaFile to cache.
     *
     * @param fileKey the Figma file key
     * @param figmaFile the FigmaFile object to cache
     */
    public void saveCachedFile(String fileKey, FigmaFile figmaFile) {
        if (fileKey == null || fileKey.isBlank() || figmaFile == null) {
            logger.warn("Cannot cache file with null or empty key or null data");
            return;
        }
        
        try {
            Path cacheFilePath = getCacheFilePath(fileKey);
            String json = gson.toJson(figmaFile);
            Files.writeString(cacheFilePath, json);
            logger.info("Cached Figma file: {} to {}", fileKey, cacheFilePath);
        } catch (IOException e) {
            logger.error("Failed to save cached file for key: {}", fileKey, e);
        }
    }

    /**
     * Loads a cached FigmaFile.
     *
     * @param fileKey the Figma file key
     * @return the cached FigmaFile, or null if not found or error occurs
     */
    public FigmaFile loadCachedFile(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }
        
        try {
            Path cacheFilePath = getCacheFilePath(fileKey);
            if (!Files.exists(cacheFilePath)) {
                logger.debug("No cache found for file key: {}", fileKey);
                return null;
            }
            
            String json = Files.readString(cacheFilePath);
            FigmaFile figmaFile = gson.fromJson(json, FigmaFile.class);
            logger.info("Loaded cached Figma file: {}", fileKey);
            return figmaFile;
        } catch (IOException e) {
            logger.error("Failed to load cached file for key: {}", fileKey, e);
            return null;
        }
    }

    /**
     * Checks if a cached file exists for the given file key.
     *
     * @param fileKey the Figma file key
     * @return true if a cache exists, false otherwise
     */
    public boolean hasCachedFile(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return false;
        }
        
        Path cacheFilePath = getCacheFilePath(fileKey);
        return Files.exists(cacheFilePath);
    }

    /**
     * Clears the cached file for the given file key.
     *
     * @param fileKey the Figma file key
     */
    public void clearCachedFile(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return;
        }
        
        try {
            Path cacheFilePath = getCacheFilePath(fileKey);
            if (Files.exists(cacheFilePath)) {
                Files.delete(cacheFilePath);
                logger.info("Cleared cache for file key: {}", fileKey);
            }
        } catch (IOException e) {
            logger.error("Failed to clear cached file for key: {}", fileKey, e);
        }
    }

    /**
     * Clears all cached files.
     */
    public void clearAllCache() {
        try {
            if (Files.exists(cacheDirectory)) {
                Files.list(cacheDirectory)
                        .filter(path -> path.toString().endsWith(CACHE_FILE_EXTENSION))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                logger.debug("Deleted cache file: {}", path);
                            } catch (IOException e) {
                                logger.error("Failed to delete cache file: {}", path, e);
                            }
                        });
                logger.info("Cleared all cache files");
            }
        } catch (IOException e) {
            logger.error("Failed to clear all cache", e);
        }
    }

    /**
     * Gets cache statistics.
     *
     * @return a map containing cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            if (Files.exists(cacheDirectory)) {
                long fileCount = Files.list(cacheDirectory)
                        .filter(path -> path.toString().endsWith(CACHE_FILE_EXTENSION))
                        .count();
                
                long totalSize = Files.list(cacheDirectory)
                        .filter(path -> path.toString().endsWith(CACHE_FILE_EXTENSION))
                        .mapToLong(path -> {
                            try {
                                return Files.size(path);
                            } catch (IOException e) {
                                return 0;
                            }
                        })
                        .sum();
                
                stats.put("fileCount", fileCount);
                stats.put("totalSizeBytes", totalSize);
                stats.put("totalSizeMB", totalSize / (1024.0 * 1024.0));
                stats.put("cacheDirectory", cacheDirectory.toString());
            } else {
                stats.put("fileCount", 0);
                stats.put("totalSizeBytes", 0);
                stats.put("totalSizeMB", 0.0);
                stats.put("cacheDirectory", cacheDirectory.toString());
            }
        } catch (IOException e) {
            logger.error("Failed to get cache stats", e);
        }
        
        return stats;
    }

    private Path getCacheFilePath(String fileKey) {
        // Sanitize file key to create a valid filename
        String sanitizedKey = fileKey.replaceAll("[^a-zA-Z0-9-_]", "_");
        return cacheDirectory.resolve(sanitizedKey + CACHE_FILE_EXTENSION);
    }
}
