package com.org.gigscore.dto;

import java.math.BigDecimal;

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
    private BigDecimal totalEarnings;
    private Integer jobsCompleted;
    private Integer activeDays;
    private DataQualityProfile dataQuality;
}