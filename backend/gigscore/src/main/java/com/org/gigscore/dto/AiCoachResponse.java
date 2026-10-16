package com.org.gigscore.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiCoachResponse {
    private String behavioralObservation;
    private String dataInsight;
    private List<String> topActions;
    private String estimatedImpactRange;
    private Confidence confidence;

    @Data
    public static class Confidence {
        private String level;
        private List<String> reasons;
    }
}
