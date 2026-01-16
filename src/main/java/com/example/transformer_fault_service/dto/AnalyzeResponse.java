package com.example.transformer_fault_service.dto;

public class AnalyzeResponse {
    private boolean received;
    private String imageUrl;
    private long imageSizeBytes;

    public AnalyzeResponse() {
    }

    public AnalyzeResponse(boolean received, String imageUrl, long imageSizeBytes) {
        this.received = received;
        this.imageUrl = imageUrl;
        this.imageSizeBytes = imageSizeBytes;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getImageSizeBytes() {
        return imageSizeBytes;
    }

    public void setImageSizeBytes(long imageSizeBytes) {
        this.imageSizeBytes = imageSizeBytes;
    }
}