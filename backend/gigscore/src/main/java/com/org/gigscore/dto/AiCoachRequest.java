package com.org.gigscore.dto;

import lombok.Data;

@Data
public class AiCoachRequest {
    private String category; // e.g., "improve_score", "increase_eligibility", etc.
}
