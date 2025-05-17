package com.progresstracking.dto.syllabus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyllabusResponse {

    private Long id;
    private String title;
    private String description;
    private boolean isPublic;
    private String shareableLink;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer subjectCount;
    private Double completionPercentage;
}
