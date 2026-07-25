package com.org.gigscore.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDashboardResponse {
    private Long userId;
    private String name;
    private String email;
    private Double totalEarnings;
    private Integer jobsCompleted;
    private Double avgRating;
    private Integer activeDays;
    private Double score;
    private List<GigSummaryResponse> gigSummaries;
}
