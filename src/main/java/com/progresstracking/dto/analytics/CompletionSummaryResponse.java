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
public class CompletionSummaryResponse {

    private Long syllabusId;
    private String syllabusTitle;
    private Double overallCompletionPercentage;
    private List<SubjectCompletionDto> subjectCompletions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectCompletionDto {
        private Long subjectId;
        private String subjectTitle;
        private Double completionPercentage;
        private Integer completedTopics;
        private Integer totalTopics;
    }
}
