package com.example.transformer_fault_service.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // We read the API key from environment variable GOOGLE_API_KEY
    private final String apiKey = System.getenv("GOOGLE_API_KEY");

    // Official REST endpoint pattern:
    // https://generativelanguage.googleapis.com/v1beta/models/<model>:generateContent :contentReference[oaicite:1]{index=1}
    private static final String MODEL = "gemini-2.5-flash";

    // Your same prompt (JSON-only output)
    private static final String PROMPT = """
You are a transformer fault diagnosis expert.

You will receive thermal images of power transformers.
These images show temperature variations where red/yellow = hot and blue = cool.

Your job:
1. Detect faulty regions and classify them. when detecting be extremely careful only to report real faults whre the hot spots is on the transformer or the equipments connected to it. because sometimes there might exist hot objects in the background that are not part of the transformer system.
2. Provide both a normalized bounding box (0–1 scale) and a rough region hint
   (top-left, top-right, bottom-left, bottom-right, or center).

Typical fault types:
- "Loose lug Connection" → small, intense, localized hotspot near terminals or joints.
- "Overload" → uniformly high temperature across windings or coils.
- "FDS Fault" or "DDLO Fault" → small, moderate to intense heating area near distribution leads. usually outside of the transformer body near transmission line side.
- "Normal" → no strong hotspots, evenly cool  or average temperature distribution.
- "Other" → any unusual patterns not fitting above categories.

Output JSON ONLY as:
[
  {
    "fault_type": "<string>",
    "bbox_normalized": [x_min, y_min, x_max, y_max],
    "region_hint": "<string>",
    "description": "<short explanation>"
  }
]

Return [] if no faults found.

Example:
Input: Bright hotspot near right terminal.
Output:
[
  {
    "fault_type": "Loose Connection",
    "bbox_normalized": [0.62, 0.34, 0.71, 0.45],
    "region_hint": "top-right",
    "description": "Localized high temperature near right terminal"
  }
]
""";

    public String analyzeImage(byte[] imageBytes, String mimeType) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GOOGLE_API_KEY environment variable is not set.");
        }

        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        // Request body for generateContent with inline image data :contentReference[oaicite:2]{index=2}
        Map<String, Object> inlineData = new LinkedHashMap<>();
        inlineData.put("mime_type", mimeType);
        inlineData.put("data", base64);

        Map<String, Object> imagePart = new LinkedHashMap<>();
        imagePart.put("inline_data", inlineData);

        Map<String, Object> textPart = new LinkedHashMap<>();
        textPart.put("text", PROMPT);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("role", "user");
        content.put("parts", List.of(textPart, imagePart));

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("temperature", 0.1);
        // JSON Mode: responseMimeType = application/json :contentReference[oaicite:3]{index=3}
        generationConfig.put("responseMimeType", "application/json");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", List.of(content));
        body.put("generationConfig", generationConfig);

        String requestJson = objectMapper.writeValueAsString(body);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + MODEL
                + ":generateContent?key="
                + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Gemini API failed. HTTP " + response.statusCode() + ": " + response.body());
        }

        // Gemini response JSON contains candidates[0].content.parts[0].text
        // In JSON mode, that "text" should itself be JSON text (like [] or [{...}]).
        JsonNode root = objectMapper.readTree(response.body());

        JsonNode textNode = root
                .path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode() || textNode.isNull()) {
            // Fallback: return the whole Gemini response if shape is unexpected
            return response.body();
        }

        return textNode.asText();
    }
}