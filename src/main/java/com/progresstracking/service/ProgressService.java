package com.progresstracking.service;

import com.progresstracking.dto.analytics.CompletionSummaryResponse;
import com.progresstracking.dto.analytics.ProgressSummaryResponse;
import com.progresstracking.dto.analytics.TimeDistributionResponse;
import com.progresstracking.dto.progress.ProgressEntryRequest;
import com.progresstracking.dto.progress.ProgressEntryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ProgressService {

    ProgressEntryResponse createProgressEntry(ProgressEntryRequest progressEntryRequest, String username);
    
    ProgressEntryResponse getProgressEntryById(Long id, String username);
    
    ProgressEntryResponse updateProgressEntry(Long id, ProgressEntryRequest progressEntryRequest, String username);
    
    void deleteProgressEntry(Long id, String username);
    
    Page<ProgressEntryResponse> getUserProgressEntries(String username, Pageable pageable);
    
    List<ProgressEntryResponse> getUserProgressEntriesByDateRange(String username, LocalDate startDate, LocalDate endDate);
    
    Double calculateSyllabusCompletionPercentage(Long syllabusId);
    
    Double calculateSubjectCompletionPercentage(Long subjectId);
    
    Double calculateTopicCompletionPercentage(Long topicId);
    
    Double calculateSubTopicCompletionPercentage(Long subTopicId);
    
    CompletionSummaryResponse getSyllabusCompletionSummary(Long syllabusId, String username);
    
    ProgressSummaryResponse getUserProgressSummary(String username, LocalDate startDate, LocalDate endDate);
    
    TimeDistributionResponse getUserTimeDistribution(String username);
    
    Integer getCurrentStreak(String username);
}
