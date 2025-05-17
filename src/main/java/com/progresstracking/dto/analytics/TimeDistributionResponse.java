package com.progresstracking.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeDistributionResponse {

    private Integer totalTimeSpentMinutes;
    private List<SubjectTimeDto> subjectDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectTimeDto {
        private Long subjectId;
        private String subjectTitle;
        private Integer timeSpentMinutes;
        private Double percentageOfTotal;
    }
}
