package com.org.gigscore.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoreResponse {
    private Long userId;
    private Double score;
    private Map<String, Double> components;
    private String band;
    private List<String> suggestions;
    private DataQualityProfile dataQuality;
}
