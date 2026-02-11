package com.example.transformer_fault_service.controller;

import com.example.transformer_fault_service.dto.AnalyzeGeminiResponse;
import com.example.transformer_fault_service.dto.AnalyzeRequest;
import com.example.transformer_fault_service.gemini.GeminiService;
import com.example.transformer_fault_service.service.ImageDownloadService;
import com.example.transformer_fault_service.service.TmsImageFaultAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

@RestController
@RequestMapping("/api/transformer")
public class TransformerAnalyzeController {

    private final ImageDownloadService imageDownloadService;
    private final GeminiService geminiService;
    private final TmsImageFaultAnalysisService analysisService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransformerAnalyzeController(
            ImageDownloadService imageDownloadService,
            GeminiService geminiService,
            TmsImageFaultAnalysisService analysisService
    ) {
        this.imageDownloadService = imageDownloadService;
        this.geminiService = geminiService;
        this.analysisService = analysisService;
    }

    @PostMapping(value = "/analyze", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnalyzeGeminiResponse> analyze(
            @RequestBody AnalyzeRequest request
    ) throws Exception {

        if (request.getImageId() == null || request.getImageId().isBlank()) {
            throw new IllegalArgumentException("imageId is required");
        }

        if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
            throw new IllegalArgumentException("imageUrl is required");
        }

        Long imageId = Long.parseLong(request.getImageId());

        // 1️⃣ Download image
        ImageDownloadService.DownloadResult download =
                imageDownloadService.downloadImage(request.getImageUrl());

        // 2️⃣ Gemini analysis
        String geminiJson =
                geminiService.analyzeImage(download.bytes(), download.contentType());

        // 3️⃣ Parse into real JSON
        JsonNode faultsNode = objectMapper.readTree(geminiJson);

        // 4️⃣ Ensure array
        if (!faultsNode.isArray()) {
            ArrayNode arr = objectMapper.createArrayNode();
            arr.add(faultsNode);
            faultsNode = arr;
        }

        // 5️⃣ ✅ SAVE / UPDATE DB (THIS IS OPTION A)
        analysisService.upsertAiResult(
                imageId,
                request.getImageUrl(),
                faultsNode
        );

        // 6️⃣ Return response
        return ResponseEntity.ok(
                new AnalyzeGeminiResponse(request.getImageId(), faultsNode)
        );
    }
}
