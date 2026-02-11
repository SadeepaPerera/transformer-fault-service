package com.example.transformer_fault_service.service;

import com.example.transformer_fault_service.dto.HumanReviewRequest;
import com.example.transformer_fault_service.model.AnalysisStatus;
import com.example.transformer_fault_service.model.TmsImageFaultAnalysis;
import com.example.transformer_fault_service.repository.TmsImageFaultAnalysisRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;

@Service
public class TmsImageFaultAnalysisService {

    private final TmsImageFaultAnalysisRepository repository;

    // ✅ Local mapper (no Spring injection needed)
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TmsImageFaultAnalysisService(TmsImageFaultAnalysisRepository repository) {
        this.repository = repository;
    }

    /**
     * UPSERT AI analysis result
     */
    @Transactional
    public TmsImageFaultAnalysis upsertAiResult(
            Long imageId,
            String imageUrl,
            JsonNode geminiResponseJson
    ) {

        TmsImageFaultAnalysis entity = repository.findByImageId(imageId)
                .orElseGet(TmsImageFaultAnalysis::new);

        entity.setImageId(imageId);
        entity.setImageUrl(imageUrl);

        // Store JSON as STRING (DB column is jsonb)
        entity.setGeminiResponse(geminiResponseJson.toString());

        // Status rule
        if (entity.getStatus() != AnalysisStatus.CONFIRMED) {
            entity.setStatus(AnalysisStatus.AI_ANALYZED);
        }

        return repository.save(entity);
    }

    /**
     * Append a human review into human_reviews (jsonb).
     * - human_reviews will be stored as a JSON array string.
     * - Each call adds one review record to the array.
     * - Status rule:
     *    - verdict == "CONFIRMED" => status = CONFIRMED
     *    - otherwise => status = HUMAN_REVIEW_PENDING
     */
    @Transactional
    public TmsImageFaultAnalysis addHumanReview(Long imageId, HumanReviewRequest req) throws Exception {

        TmsImageFaultAnalysis entity = repository.findByImageId(imageId)
                .orElseThrow(() -> new IllegalArgumentException("No analysis found for imageId=" + imageId));

        // 1) Load existing human_reviews as JSON array (or create empty)
        ArrayNode reviewsArray;
        String existing = entity.getHumanReviews();

        if (existing == null || existing.isBlank()) {
            reviewsArray = objectMapper.createArrayNode();
        } else {
            JsonNode existingNode = objectMapper.readTree(existing);

            if (existingNode.isArray()) {
                reviewsArray = (ArrayNode) existingNode;
            } else {
                // If it was accidentally stored as a single object, wrap it into array
                reviewsArray = objectMapper.createArrayNode();
                reviewsArray.add(existingNode);
            }
        }

        // 2) Build one review record
        ObjectNode review = objectMapper.createObjectNode();

        if (req.getReviewer() != null && !req.getReviewer().isBlank()) {
            review.put("reviewer", req.getReviewer());
        }

        if (req.getVerdict() != null && !req.getVerdict().isBlank()) {
            review.put("verdict", req.getVerdict());
        }

        if (req.getNotes() != null && !req.getNotes().isBlank()) {
            review.put("notes", req.getNotes());
        }

        review.put("reviewed_at", Instant.now().toString());

        // faults can be array/object; store as-is
        if (req.getFaults() != null) {
            review.set("faults", req.getFaults());
        } else {
            review.set("faults", objectMapper.createArrayNode());
        }

        // 3) Append to array
        reviewsArray.add(review);

        // 4) Save back as STRING JSON
        entity.setHumanReviews(reviewsArray.toString());

        // 5) Status logic
        if (req.getVerdict() != null && "CONFIRMED".equalsIgnoreCase(req.getVerdict())) {
            entity.setStatus(AnalysisStatus.CONFIRMED);
        } else {
            entity.setStatus(AnalysisStatus.HUMAN_REVIEW_PENDING);
        }

        return repository.save(entity);
    }
}
