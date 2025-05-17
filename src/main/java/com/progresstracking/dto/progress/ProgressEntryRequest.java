package com.progresstracking.dto.progress;

import com.progresstracking.model.ProgressEntry;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressEntryRequest {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Item type is required")
    private ProgressEntry.ItemType itemType;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Status is required")
    private ProgressEntry.Status status;

    @Min(value = 0, message = "Time spent must be a positive number")
    private Integer timeSpentMinutes;

    private String notes;
}
