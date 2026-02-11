package com.example.transformer_fault_service.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "tms_image_fault_analysis")
public class TmsImageFaultAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_id", nullable = false, unique = true)
    private Long imageId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // ✅ Correct mapping for PostgreSQL named enum type
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "tms_analysis_status_enum")
    private AnalysisStatus status = AnalysisStatus.AI_ANALYZED;

    // ✅ Store as String; DB column is jsonb
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gemini_response", nullable = false, columnDefinition = "jsonb")
    private String geminiResponse;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "human_reviews", columnDefinition = "jsonb")
    private String humanReviews;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // getters / setters
    public Long getId() { return id; }

    public Long getImageId() { return imageId; }
    public void setImageId(Long imageId) { this.imageId = imageId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public AnalysisStatus getStatus() { return status; }
    public void setStatus(AnalysisStatus status) { this.status = status; }

    public String getGeminiResponse() { return geminiResponse; }
    public void setGeminiResponse(String geminiResponse) { this.geminiResponse = geminiResponse; }

    public String getHumanReviews() { return humanReviews; }
    public void setHumanReviews(String humanReviews) { this.humanReviews = humanReviews; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
