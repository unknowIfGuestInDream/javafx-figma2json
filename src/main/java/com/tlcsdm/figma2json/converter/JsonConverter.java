package com.tlcsdm.figma2json.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.tlcsdm.figma2json.api.Node;

/**
 * Converter for exporting Figma data as formatted JSON.
 */
public class JsonConverter implements FigmaConverter {

    private final Gson gson;

    public JsonConverter() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    @Override
    public String getFormat() {
        return "JSON";
    }

    @Override
    public String convert(Node node) {
        return gson.toJson(node);
    }

    @Override
    public String convertRawJson(String rawJson) {
        // Parse and re-format with pretty printing
        JsonElement element = JsonParser.parseString(rawJson);
        return gson.toJson(element);
    }

    @Override
    public String getFileExtension() {
        return "json";
    }
}
