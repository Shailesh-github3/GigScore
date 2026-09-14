package com.org.gigscore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LenderWorkerSummary {
    private Long userId;
    private String name;
    private String email;
    private Double score;
    private String band;
    private Double totalEarnings;
    private Integer jobsCompleted;
    private Integer activeDays;
    private DataQualityProfile dataQuality;
}