package com.example.transformer_fault_service.controller;

import com.example.transformer_fault_service.dto.AnalyzeRequest;
import com.example.transformer_fault_service.gemini.GeminiService;
import com.example.transformer_fault_service.service.ImageDownloadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transformer")
public class TransformerAnalyzeController {

    private final ImageDownloadService imageDownloadService;
    private final GeminiService geminiService;

    public TransformerAnalyzeController(ImageDownloadService imageDownloadService, GeminiService geminiService) {
        this.imageDownloadService = imageDownloadService;
        this.geminiService = geminiService;
    }

    @PostMapping(value = "/analyze", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> analyze(@RequestBody AnalyzeRequest request) throws Exception {

        // 1) Download image from Blob URL
        ImageDownloadService.DownloadResult download = imageDownloadService.downloadImage(request.getImageUrl());

        // 2) Send image bytes + prompt to Gemini, get JSON text
        String geminiJson = geminiService.analyzeImage(download.bytes(), download.contentType());

        // 3) Return Gemini JSON exactly as-is
        return ResponseEntity.ok(geminiJson);
    }
}