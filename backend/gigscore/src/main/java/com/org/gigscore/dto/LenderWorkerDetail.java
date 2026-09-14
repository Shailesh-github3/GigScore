package com.org.gigscore.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LenderWorkerDetail {
    private Long userId;
    private String name;
    private String email;
    private Double score;
    private String band;
    private Map<String, Double> components;
    private List<String> suggestions;
    private Double totalEarnings;
    private Integer jobsCompleted;
    private Integer activeDays;
    private DataQualityProfile dataQuality;
    private List<ActivityResponse> recentActivities;
}