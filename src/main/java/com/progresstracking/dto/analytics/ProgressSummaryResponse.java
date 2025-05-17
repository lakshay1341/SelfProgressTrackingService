package com.progresstracking.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressSummaryResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDaysWithProgress;
    private Integer totalTimeSpentMinutes;
    private Integer streak;
    private List<DailyProgressDto> dailyProgress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyProgressDto {
        private LocalDate date;
        private Integer timeSpentMinutes;
        private Integer itemsProgressed;
    }
}
