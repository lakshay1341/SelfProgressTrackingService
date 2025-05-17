package com.progresstracking.dto.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {

    private Long id;
    private Long syllabusId;
    private String title;
    private String description;
    private Integer order;
    private LocalDate targetCompletionDate;
    private Integer topicCount;
    private Double completionPercentage;
}
