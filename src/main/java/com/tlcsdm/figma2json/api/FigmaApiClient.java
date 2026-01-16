package com.tlcsdm.figma2json.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Client for Figma REST API using JDK HttpClient.
 */
public class FigmaApiClient {

    private static final String BASE_URL = "https://api.figma.com/v1";

    private final HttpClient httpClient;
    private final Gson gson;
    private String accessToken;

    public FigmaApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Sets the personal access token for API authentication.
     *
     * @param accessToken the personal access token
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Gets a Figma file by its key.
     *
     * @param fileKey the file key extracted from the Figma URL
     * @return CompletableFuture containing the FigmaFile
     */
    public CompletableFuture<FigmaFile> getFile(String fileKey) {
        String url = BASE_URL + "/files/" + fileKey;
        HttpRequest request = buildRequest(url);

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("API request failed with status: " + response.statusCode() + 
                                ", body: " + response.body());
                    }
                    return gson.fromJson(response.body(), FigmaFile.class);
                });
    }

    /**
     * Gets a specific node from a Figma file.
     *
     * @param fileKey the file key
     * @param nodeId  the node ID
     * @return CompletableFuture containing the node data as JSON string
     */
    public CompletableFuture<String> getNode(String fileKey, String nodeId) {
        String url = BASE_URL + "/files/" + fileKey + "/nodes?ids=" + nodeId;
        HttpRequest request = buildRequest(url);

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("API request failed with status: " + response.statusCode() +
                                ", body: " + response.body());
                    }
                    return response.body();
                });
    }

    /**
     * Gets file synchronously for testing.
     *
     * @param fileKey the file key
     * @return the FigmaFile
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public FigmaFile getFileSync(String fileKey) throws IOException, InterruptedException {
        String url = BASE_URL + "/files/" + fileKey;
        HttpRequest request = buildRequest(url);

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("API request failed with status: " + response.statusCode() +
                    ", body: " + response.body());
        }
        return gson.fromJson(response.body(), FigmaFile.class);
    }

    /**
     * Extracts the file key from a Figma URL.
     *
     * @param figmaUrl the full Figma URL
     * @return the extracted file key
     */
    public static String extractFileKey(String figmaUrl) {
        // Figma URLs format: https://www.figma.com/file/{file_key}/{file_name}
        // or https://www.figma.com/design/{file_key}/{file_name}
        if (figmaUrl == null || figmaUrl.isBlank()) {
            return null;
        }
        String[] parts = figmaUrl.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("file".equals(parts[i]) || "design".equals(parts[i])) {
                if (i + 1 < parts.length) {
                    String fileKey = parts[i + 1];
                    // Remove any query parameters
                    int queryIndex = fileKey.indexOf('?');
                    if (queryIndex > 0) {
                        fileKey = fileKey.substring(0, queryIndex);
                    }
                    return fileKey;
                }
            }
        }
        return null;
    }

    private HttpRequest buildRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-FIGMA-TOKEN", accessToken)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
    }

    public Gson getGson() {
        return gson;
    }
}
