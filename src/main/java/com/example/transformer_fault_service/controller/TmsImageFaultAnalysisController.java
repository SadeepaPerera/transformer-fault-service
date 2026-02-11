package com.example.transformer_fault_service.controller;

import com.example.transformer_fault_service.dto.HumanReviewRequest;
import com.example.transformer_fault_service.repository.TmsImageFaultAnalysisRepository;
import com.example.transformer_fault_service.service.TmsImageFaultAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class TmsImageFaultAnalysisController {

    private final TmsImageFaultAnalysisRepository repo;
    private final TmsImageFaultAnalysisService service;

    public TmsImageFaultAnalysisController(TmsImageFaultAnalysisRepository repo,
                                           TmsImageFaultAnalysisService service) {
        this.repo = repo;
        this.service = service;
    }

    // ✅ existing GET (no change in behavior)
    @GetMapping("/{imageId}")
    public ResponseEntity<?> getByImageId(@PathVariable Long imageId) {

        return repo.findByImageId(imageId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(
                        Map.of(
                                "found", false,
                                "imageId", imageId,
                                "message", "No analysis found for this imageId"
                        )
                ));
    }

    // ✅ STEP 3: Human review endpoint
    @PostMapping("/{imageId}/human-review")
    public ResponseEntity<?> addHumanReview(
            @PathVariable Long imageId,
            @RequestBody HumanReviewRequest request
    ) throws Exception {

        var saved = service.addHumanReview(imageId, request);

        return ResponseEntity.ok(Map.of(
                "saved", true,
                "imageId", saved.getImageId(),
                "dbId", saved.getId(),
                "status", saved.getStatus().name()
        ));
    }
}
