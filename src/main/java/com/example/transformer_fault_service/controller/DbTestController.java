package com.example.transformer_fault_service.controller;

import com.example.transformer_fault_service.model.AnalysisStatus;
import com.example.transformer_fault_service.model.TmsImageFaultAnalysis;
import com.example.transformer_fault_service.repository.TmsImageFaultAnalysisRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RestController
@RequestMapping("/api/db-test")
public class DbTestController {

    private final TmsImageFaultAnalysisRepository repo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DbTestController(TmsImageFaultAnalysisRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/upsert")
    public ResponseEntity<?> upsert(@RequestBody Map<String, Object> body) throws Exception {

        Long imageId = Long.valueOf(body.get("imageId").toString());
        String imageUrl = body.get("imageUrl").toString();
        String geminiResponseStr = body.get("geminiResponse").toString();

        // ✅ validate it is JSON
        objectMapper.readTree(geminiResponseStr);

        TmsImageFaultAnalysis entity = repo.findByImageId(imageId)
                .orElseGet(TmsImageFaultAnalysis::new);

        entity.setImageId(imageId);
        entity.setImageUrl(imageUrl);
        entity.setStatus(AnalysisStatus.AI_ANALYZED);
        entity.setGeminiResponse(geminiResponseStr);

        TmsImageFaultAnalysis saved = repo.save(entity);

        return ResponseEntity.ok(Map.of(
                "saved", true,
                "dbId", saved.getId(),
                "imageId", saved.getImageId()
        ));
    }
}
