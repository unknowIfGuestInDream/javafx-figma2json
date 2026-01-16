package com.tlcsdm.figma2json.api;

/**
 * Represents a Figma component.
 */
public class Component {
    private String key;
    private String name;
    private String description;
    private String componentSetId;
    private String documentationLinks;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComponentSetId() {
        return componentSetId;
    }

    public void setComponentSetId(String componentSetId) {
        this.componentSetId = componentSetId;
    }

    public String getDocumentationLinks() {
        return documentationLinks;
    }

    public void setDocumentationLinks(String documentationLinks) {
        this.documentationLinks = documentationLinks;
    }
}
