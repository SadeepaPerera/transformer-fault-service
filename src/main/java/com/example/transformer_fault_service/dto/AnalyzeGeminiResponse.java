package com.example.transformer_fault_service.dto;

import tools.jackson.databind.JsonNode;

public class AnalyzeGeminiResponse {
    private String imageId;
    private JsonNode faults;

    public AnalyzeGeminiResponse() {}

    public AnalyzeGeminiResponse(String imageId, JsonNode faults) {
        this.imageId = imageId;
        this.faults = faults;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public JsonNode getFaults() {
        return faults;
    }

    public void setFaults(JsonNode faults) {
        this.faults = faults;
    }
}
