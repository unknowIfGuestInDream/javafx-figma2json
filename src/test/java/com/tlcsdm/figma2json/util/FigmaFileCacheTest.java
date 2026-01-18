package com.tlcsdm.figma2json.util;

import com.tlcsdm.figma2json.api.Document;
import com.tlcsdm.figma2json.api.FigmaFile;
import com.tlcsdm.figma2json.api.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FigmaFileCache.
 */
class FigmaFileCacheTest {

    private FigmaFileCache cache;
    private static final String TEST_FILE_KEY = "test-file-key-123";

    @BeforeEach
    void setUp() {
        cache = new FigmaFileCache();
        // Clear any existing test cache
        cache.clearCachedFile(TEST_FILE_KEY);
    }

    @AfterEach
    void tearDown() {
        // Clean up test cache
        cache.clearCachedFile(TEST_FILE_KEY);
    }

    @Test
    @DisplayName("saveCachedFile should save a file to cache")
    void saveCachedFile_validFile_savesSuccessfully() {
        FigmaFile file = createTestFigmaFile();
        
        cache.saveCachedFile(TEST_FILE_KEY, file);
        
        assertTrue(cache.hasCachedFile(TEST_FILE_KEY));
    }

    @Test
    @DisplayName("loadCachedFile should load a saved file")
    void loadCachedFile_existingFile_loadsSuccessfully() {
        FigmaFile originalFile = createTestFigmaFile();
        cache.saveCachedFile(TEST_FILE_KEY, originalFile);
        
        FigmaFile loadedFile = cache.loadCachedFile(TEST_FILE_KEY);
        
        assertNotNull(loadedFile);
        assertEquals(originalFile.getName(), loadedFile.getName());
        assertEquals(originalFile.getVersion(), loadedFile.getVersion());
    }

    @Test
    @DisplayName("loadCachedFile should return null for non-existent file")
    void loadCachedFile_nonExistentFile_returnsNull() {
        FigmaFile file = cache.loadCachedFile("non-existent-key");
        
        assertNull(file);
    }

    @Test
    @DisplayName("hasCachedFile should return true for cached file")
    void hasCachedFile_cachedFile_returnsTrue() {
        FigmaFile file = createTestFigmaFile();
        cache.saveCachedFile(TEST_FILE_KEY, file);
        
        assertTrue(cache.hasCachedFile(TEST_FILE_KEY));
    }

    @Test
    @DisplayName("hasCachedFile should return false for non-cached file")
    void hasCachedFile_nonCachedFile_returnsFalse() {
        assertFalse(cache.hasCachedFile("non-existent-key"));
    }

    @Test
    @DisplayName("clearCachedFile should remove cached file")
    void clearCachedFile_existingFile_removesFile() {
        FigmaFile file = createTestFigmaFile();
        cache.saveCachedFile(TEST_FILE_KEY, file);
        assertTrue(cache.hasCachedFile(TEST_FILE_KEY));
        
        cache.clearCachedFile(TEST_FILE_KEY);
        
        assertFalse(cache.hasCachedFile(TEST_FILE_KEY));
    }

    @Test
    @DisplayName("clearAllCache should remove all cached files")
    void clearAllCache_multipleFiles_removesAll() {
        FigmaFile file1 = createTestFigmaFile();
        FigmaFile file2 = createTestFigmaFile();
        cache.saveCachedFile("key1", file1);
        cache.saveCachedFile("key2", file2);
        
        cache.clearAllCache();
        
        assertFalse(cache.hasCachedFile("key1"));
        assertFalse(cache.hasCachedFile("key2"));
    }

    @Test
    @DisplayName("getCacheStats should return valid statistics")
    void getCacheStats_withCachedFiles_returnsValidStats() {
        FigmaFile file = createTestFigmaFile();
        cache.saveCachedFile(TEST_FILE_KEY, file);
        
        Map<String, Object> stats = cache.getCacheStats();
        
        assertNotNull(stats);
        assertTrue(stats.containsKey("fileCount"));
        assertTrue(stats.containsKey("totalSizeBytes"));
        assertTrue(stats.containsKey("cacheDirectory"));
        assertTrue((Long) stats.get("fileCount") >= 1);
    }

    @Test
    @DisplayName("saveCachedFile should handle null file key")
    void saveCachedFile_nullFileKey_doesNotThrow() {
        FigmaFile file = createTestFigmaFile();
        
        assertDoesNotThrow(() -> cache.saveCachedFile(null, file));
    }

    @Test
    @DisplayName("saveCachedFile should handle null file")
    void saveCachedFile_nullFile_doesNotThrow() {
        assertDoesNotThrow(() -> cache.saveCachedFile(TEST_FILE_KEY, null));
    }

    @Test
    @DisplayName("loadCachedFile should handle null file key")
    void loadCachedFile_nullFileKey_returnsNull() {
        FigmaFile file = cache.loadCachedFile(null);
        
        assertNull(file);
    }

    @Test
    @DisplayName("cached file should preserve document structure")
    void cachedFile_withDocument_preservesStructure() {
        FigmaFile originalFile = createTestFigmaFile();
        cache.saveCachedFile(TEST_FILE_KEY, originalFile);
        
        FigmaFile loadedFile = cache.loadCachedFile(TEST_FILE_KEY);
        
        assertNotNull(loadedFile.getDocument());
        assertNotNull(loadedFile.getDocument().getChildren());
        assertEquals(originalFile.getDocument().getChildren().size(), 
                     loadedFile.getDocument().getChildren().size());
    }

    private FigmaFile createTestFigmaFile() {
        FigmaFile file = new FigmaFile();
        file.setName("Test Figma File");
        file.setVersion("1.0");
        file.setLastModified("2026-01-18T00:00:00Z");
        
        Document document = new Document();
        document.setId("doc-123");
        document.setName("Document");
        document.setType("DOCUMENT");
        
        List<Node> pages = new ArrayList<>();
        Node page = new Node();
        page.setId("page-1");
        page.setName("Page 1");
        page.setType("CANVAS");
        
        List<Node> layers = new ArrayList<>();
        Node layer = new Node();
        layer.setId("layer-1");
        layer.setName("Layer 1");
        layer.setType("FRAME");
        page.setChildren(layers);
        layers.add(layer);
        
        pages.add(page);
        document.setChildren(pages);
        file.setDocument(document);
        
        return file;
    }
}
