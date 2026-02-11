package com.example.transformer_fault_service.dto;

public class AnalyzeRequest {
    private String imageId;
    private String imageUrl;

    public AnalyzeRequest() {}

    public AnalyzeRequest(String imageId, String imageUrl) {
        this.imageId = imageId;
        this.imageUrl = imageUrl;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
