package com.tlcsdm.figma2json.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Represents a Figma node (Canvas, Frame, Group, etc.).
 */
public class Node {
    private String id;
    private String name;
    private String type;
    private Boolean visible;
    private List<Node> children;

    @SerializedName("absoluteBoundingBox")
    private BoundingBox absoluteBoundingBox;

    private Map<String, Object> fills;
    private Map<String, Object> strokes;
    private Double strokeWeight;
    private Double cornerRadius;
    private Map<String, Object> effects;
    private String blendMode;
    private Double opacity;
    private Map<String, Object> constraints;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public BoundingBox getAbsoluteBoundingBox() {
        return absoluteBoundingBox;
    }

    public void setAbsoluteBoundingBox(BoundingBox absoluteBoundingBox) {
        this.absoluteBoundingBox = absoluteBoundingBox;
    }

    public Map<String, Object> getFills() {
        return fills;
    }

    public void setFills(Map<String, Object> fills) {
        this.fills = fills;
    }

    public Map<String, Object> getStrokes() {
        return strokes;
    }

    public void setStrokes(Map<String, Object> strokes) {
        this.strokes = strokes;
    }

    public Double getStrokeWeight() {
        return strokeWeight;
    }

    public void setStrokeWeight(Double strokeWeight) {
        this.strokeWeight = strokeWeight;
    }

    public Double getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(Double cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    public Map<String, Object> getEffects() {
        return effects;
    }

    public void setEffects(Map<String, Object> effects) {
        this.effects = effects;
    }

    public String getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(String blendMode) {
        this.blendMode = blendMode;
    }

    public Double getOpacity() {
        return opacity;
    }

    public void setOpacity(Double opacity) {
        this.opacity = opacity;
    }

    public Map<String, Object> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<String, Object> constraints) {
        this.constraints = constraints;
    }

    @Override
    public String toString() {
        return name != null ? name : id;
    }
}
