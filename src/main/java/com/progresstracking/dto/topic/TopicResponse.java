package com.progresstracking.dto.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponse {

    private Long id;
    private Long subjectId;
    private String title;
    private String description;
    private Integer order;
    private LocalDate targetCompletionDate;
    private Integer subTopicCount;
    private Double completionPercentage;
}
