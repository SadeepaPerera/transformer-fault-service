# Transformer Fault Analysis Service

---

## Overview

This is a Spring Boot microservice that performs AI-based transformer thermal fault detection using Google Gemini 2.5 Flash, and stores:

- AI analysis results
- Human review decisions
- Status lifecycle tracking

This service is designed to integrate into a larger Transformer Management System (TMS).

---

# What This Service Does

1. Accepts a public thermal image URL
2. Downloads the image into memory
3. Sends image + structured prompt to Gemini 2.5 Flash
4. Receives structured JSON fault detection results
5. Stores AI results in PostgreSQL (JSONB)
6. Allows human reviewers to confirm or override AI results
7. Stores human review decisions in JSONB
8. Maintains fault lifecycle status

---

# Architecture

## Backend
- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL 17

## AI Model
- Google Gemini 2.5 Flash

## Storage
- JSONB columns (AI + Human review)
- Enum-based status management
- Indexed database design

## Image Source
- Public Azure Blob Storage URLs

---

# Database Design

## Enum

```sql
CREATE TYPE tms_analysis_status_enum AS ENUM (
  'AI_ANALYZED',
  'HUMAN_REVIEW_PENDING',
  'CONFIRMED'
);
```

---

## Sequence

```sql
CREATE SEQUENCE tms_image_fault_analysis_id_seq
  START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
```

---

## Table

```sql
CREATE TABLE public.tms_image_fault_analysis (
  id bigint NOT NULL DEFAULT nextval('tms_image_fault_analysis_id_seq'::regclass),
  image_id bigint NOT NULL,
  image_url text NOT NULL,
  status tms_analysis_status_enum NOT NULL DEFAULT 'AI_ANALYZED',
  gemini_response jsonb NOT NULL,
  human_reviews jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
```

---

## Indexes

```sql
CREATE UNIQUE INDEX uq_tms_image_id
ON public.tms_image_fault_analysis (image_id);

CREATE INDEX idx_tms_fault_status
ON public.tms_image_fault_analysis (status);
```

---

# Status Lifecycle

| Status | Meaning |
|--------|----------|
| AI_ANALYZED | Gemini analysis saved |
| HUMAN_REVIEW_PENDING | Awaiting human review |
| CONFIRMED | Human confirmed AI result |

---

# API Endpoints

---

## 1. Analyze Transformer Image

POST  
`/api/transformer/analyze`

### Request

```json
{
  "imageId": 296,
  "imageUrl": "https://public-blob-url/image.jpg"
}
```

### Behavior

- Downloads image
- Calls Gemini
- Saves result in DB
- Returns AI response

---

## 2. Get Analysis Result

GET  
`/api/analysis/{imageId}`

Example:

```
GET /api/analysis/296
```

Returns full DB record.

---

## 3. Submit Human Review

POST  
`/api/analysis/{imageId}/human-review`

### Request

```json
{
  "reviewer": "sadeepa",
  "verdict": "CONFIRMED",
  "notes": "Hotspot confirmed at lug connection.",
  "faults": [
    {
      "fault_type": "Loose lug Connection",
      "bbox_normalized": [0.48, 0.43, 0.58, 0.53],
      "region_hint": "center",
      "description": "Confirmed hotspot"
    }
  ]
}
```

### Behavior

- Appends review into JSONB
- Updates status
- Updates updated_at timestamp

---

# Environment Configuration

## Required Environment Variable

Google Gemini API key must NOT be hardcoded.

### Windows (PowerShell)

```powershell
setx GOOGLE_API_KEY "YOUR_API_KEY"
```

Restart IDE after setting.

---

## Database Configuration

`application.yml` uses environment variables:

- DB_URL
- DB_USER
- DB_PASSWORD

Example:

```powershell
setx DB_PASSWORD "your_password"
```

---

# How To Run Locally

### 1. Set Spring Profile

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
```

---

### 2. Run Application

```powershell
.\mvnw.cmd spring-boot:run
```

Service runs at:

```
http://localhost:8080
```

Health check:

```
http://localhost:8080/actuator/health
```

---

# Design Principles

- Stateless microservice
- JSONB storage for flexibility
- Enum-based lifecycle tracking
- Idempotent upsert (unique image_id)
- No image files stored
- AI responses preserved
- Human review history preserved
- Production-ready DB indexing

---

# Integration Notes

- Call `/api/transformer/analyze` after image upload
- Use `/api/analysis/{imageId}` to retrieve stored results
- Render bounding boxes using normalized coordinates (0–1 range)
- Use human review endpoint to finalize decision

---

# Completion Status

| Module | Status |
|--------|--------|
| Gemini AI Integration | Complete |
| DB Persistence | Complete |
| Human Review Append | Complete |
| Status Lifecycle | Complete |
| Indexing | Complete |
| Local Testing | Complete |
| GitHub Deployment | Complete |

---

# Future Improvements (Optional)

- Add DB trigger for automatic updated_at update
- Add pagination endpoint
- Add authentication layer
- Add audit logging
- Add soft-delete flag

---

# Developer

Sadeepa  
Transformer Fault AI Module  
TMS Integration Service

---

# Ready for Integration

This module is ready for integration into the main Transformer Management System.
