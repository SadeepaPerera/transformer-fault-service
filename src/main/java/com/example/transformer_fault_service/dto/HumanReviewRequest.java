package com.example.transformer_fault_service.dto;

import tools.jackson.databind.JsonNode;

public class HumanReviewRequest {

    private String reviewer;    // optional
    private String verdict;     // "CONFIRMED" or "PENDING"
    private JsonNode faults;    // array or object
    private String notes;       // optional

    public HumanReviewRequest() {}

    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

    public JsonNode getFaults() { return faults; }
    public void setFaults(JsonNode faults) { this.faults = faults; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
