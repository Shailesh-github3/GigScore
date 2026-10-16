package com.org.gigscore.dto;

import java.math.BigDecimal;

public interface WorkerSummaryProjection {
    Long getUserId();
    String getName();
    String getEmail();
    Double getScore();
    BigDecimal getTotalEarnings();
    Long getJobsCompleted();
    Long getActiveDays();
}
