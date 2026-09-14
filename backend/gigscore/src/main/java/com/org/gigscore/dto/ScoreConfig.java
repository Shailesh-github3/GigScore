package com.org.gigscore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoreConfig {
    private Double maxScore;
    private Double earningsTarget;
    private Double jobsTarget;
    private Double ratingMax;
    private Double activeDaysTarget;
    private WeightSet weights;
    private ExponentSet exponents;

    @Data
    @Builder
    public static class WeightSet {
        private Double earnings;
        private Double jobs;
        private Double rating;
        private Double activeDays;
    }

    @Data
    @Builder
    public static class ExponentSet {
        private Double earnings;
        private Double jobs;
        private Double rating;
        private Double activeDays;
    }
}
