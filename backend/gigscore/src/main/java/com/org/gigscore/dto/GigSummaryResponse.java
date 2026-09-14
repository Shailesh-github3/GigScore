package com.org.gigscore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GigSummaryResponse {
    private String platform;
    private Double totalEarnings;
    private Integer jobsCompleted;
    private Double avgRating;
    private Integer activeDays;
}
