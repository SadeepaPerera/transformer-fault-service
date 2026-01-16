package com.example.transformer_fault_service.dto;

public class AnalyzeRequest {
    private String imageUrl;

    public AnalyzeRequest() {
    }

    public AnalyzeRequest(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
