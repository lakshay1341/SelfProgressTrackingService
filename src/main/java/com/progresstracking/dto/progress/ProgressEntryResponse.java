package com.progresstracking.dto.progress;

import com.progresstracking.model.ProgressEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressEntryResponse {

    private Long id;
    private Long userId;
    private Long itemId;
    private ProgressEntry.ItemType itemType;
    private String itemTitle;
    private LocalDate date;
    private ProgressEntry.Status status;
    private Integer timeSpentMinutes;
    private String notes;
}
