package com.org.gigscore.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GigSummaryResponse {
    private String platform;
    private BigDecimal totalEarnings;
    private Integer jobsCompleted;
    private Double avgRating;
}
