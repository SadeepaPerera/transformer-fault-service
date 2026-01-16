# Transformer Fault Detection Service

This is a **Spring Boot microservice** that performs **thermal transformer fault detection** using **Google Gemini 2.5 Flash**.

The service receives a **public image URL** (stored in Azure Blob Storage), sends the image to Gemini with a structured prompt, and returns **JSON fault detection results**.

---

## What this service does (simple explanation)

1. Receives a POST request with an image URL
2. Downloads the image into memory (not saved on disk)
3. Sends the image + prompt to Gemini 2.5 Flash
4. Receives fault detection results as JSON
5. Returns the JSON response to the caller

The service is **stateless**, **fast**, and **cloud-ready**.

---

## Architecture overview

- Backend: Spring Boot (Java)
- AI Model: Google Gemini 2.5 Flash
- Image Source: Azure Blob Storage (public URLs)
- Output: JSON (no image modification)

This service **does NOT**:
- store images
- draw bounding boxes
- save results in a database

Bounding boxes are returned as **normalized coordinates** and should be rendered by the frontend.

---

## API Endpoint

### Analyze Transformer Image

**POST**

/api/transformer/analyze


### Request body
```json
{
  "imageUrl": "https://your-blob-url/image.jpg"
}

Response (example)

[
  {
    "fault_type": "Loose lug Connection",
    "bbox_normalized": [0.52, 0.20, 0.60, 0.30],
    "region_hint": "top-center",
    "description": "Intense localized hotspot detected at a connection point."
  }
]

If no faults are detected:

[]

Environment Variable (REQUIRED)

The Gemini API key must NOT be hardcoded.

Set it as an environment variable:

Windows (PowerShell as Administrator)
setx GOOGLE_API_KEY "YOUR_API_KEY_HERE"


Restart IntelliJ / application after setting the key.

How to run locally
Prerequisites

Java 17 or later

Maven (included via wrapper)

Run
./mvnw spring-boot:run


or run TransformerFaultServiceApplication from IntelliJ.

The service starts on:

http://localhost:8080


Health check:

http://localhost:8080/actuator/health

Notes for frontend / main backend developers

Send only the image URL

The service handles downloading and Gemini calls

Use bbox_normalized values to draw boxes on the frontend

Coordinates are normalized (0–1 range)

The service returns pure JSON only

