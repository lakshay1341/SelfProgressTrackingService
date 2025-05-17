package com.progresstracking.dto.subtopic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubTopicResponse {

    private Long id;
    private Long topicId;
    private String title;
    private String description;
    private Integer order;
    private LocalDate targetCompletionDate;
    private Double completionPercentage;
}
