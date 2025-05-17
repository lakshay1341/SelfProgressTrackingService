package com.progresstracking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.progresstracking.dto.analytics.CompletionSummaryResponse;
import com.progresstracking.dto.analytics.ProgressSummaryResponse;
import com.progresstracking.dto.analytics.TimeDistributionResponse;
import com.progresstracking.dto.progress.ProgressEntryRequest;
import com.progresstracking.dto.progress.ProgressEntryResponse;
import com.progresstracking.model.ProgressEntry;
import com.progresstracking.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProgressController.class)
public class ProgressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProgressService progressService;

    private ProgressEntryRequest progressEntryRequest;
    private ProgressEntryResponse progressEntryResponse;
    private CompletionSummaryResponse completionSummaryResponse;
    private ProgressSummaryResponse progressSummaryResponse;
    private TimeDistributionResponse timeDistributionResponse;

    @BeforeEach
    void setUp() {
        progressEntryRequest = ProgressEntryRequest.builder()
                .itemId(1L)
                .itemType(ProgressEntry.ItemType.SUBJECT)
                .date(LocalDate.now())
                .status(ProgressEntry.Status.IN_PROGRESS)
                .timeSpentMinutes(60)
                .notes("Test notes")
                .build();

        progressEntryResponse = ProgressEntryResponse.builder()
                .id(1L)
                .userId(1L)
                .itemId(1L)
                .itemType(ProgressEntry.ItemType.SUBJECT)
                .itemTitle("Test Subject")
                .date(LocalDate.now())
                .status(ProgressEntry.Status.IN_PROGRESS)
                .timeSpentMinutes(60)
                .notes("Test notes")
                .build();

        completionSummaryResponse = CompletionSummaryResponse.builder()
                .syllabusId(1L)
                .syllabusTitle("Test Syllabus")
                .overallCompletionPercentage(50.0)
                .subjectCompletions(Collections.singletonList(
                        CompletionSummaryResponse.SubjectCompletionDto.builder()
                                .subjectId(1L)
                                .subjectTitle("Test Subject")
                                .completionPercentage(50.0)
                                .completedTopics(1)
                                .totalTopics(2)
                                .build()
                ))
                .build();

        progressSummaryResponse = ProgressSummaryResponse.builder()
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now())
                .totalDaysWithProgress(3)
                .totalTimeSpentMinutes(180)
                .streak(2)
                .dailyProgress(Collections.singletonList(
                        ProgressSummaryResponse.DailyProgressDto.builder()
                                .date(LocalDate.now())
                                .timeSpentMinutes(60)
                                .itemsProgressed(1)
                                .build()
                ))
                .build();

        timeDistributionResponse = TimeDistributionResponse.builder()
                .totalTimeSpentMinutes(180)
                .subjectDistribution(Collections.singletonList(
                        TimeDistributionResponse.SubjectTimeDto.builder()
                                .subjectId(1L)
                                .subjectTitle("Test Subject")
                                .timeSpentMinutes(180)
                                .percentageOfTotal(100.0)
                                .build()
                ))
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void createProgressEntry_Success() throws Exception {
        // Arrange
        when(progressService.createProgressEntry(any(ProgressEntryRequest.class), anyString())).thenReturn(progressEntryResponse);

        // Act & Assert
        mockMvc.perform(post("/progress")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(progressEntryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.itemId").value(1))
                .andExpect(jsonPath("$.itemType").value("SUBJECT"))
                .andExpect(jsonPath("$.itemTitle").value("Test Subject"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.timeSpentMinutes").value(60))
                .andExpect(jsonPath("$.notes").value("Test notes"));

        verify(progressService).createProgressEntry(any(ProgressEntryRequest.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getProgressEntry_Success() throws Exception {
        // Arrange
        when(progressService.getProgressEntryById(anyLong(), anyString())).thenReturn(progressEntryResponse);

        // Act & Assert
        mockMvc.perform(get("/progress/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.itemId").value(1))
                .andExpect(jsonPath("$.itemType").value("SUBJECT"))
                .andExpect(jsonPath("$.itemTitle").value("Test Subject"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.timeSpentMinutes").value(60))
                .andExpect(jsonPath("$.notes").value("Test notes"));

        verify(progressService).getProgressEntryById(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateProgressEntry_Success() throws Exception {
        // Arrange
        when(progressService.updateProgressEntry(anyLong(), any(ProgressEntryRequest.class), anyString())).thenReturn(progressEntryResponse);

        // Act & Assert
        mockMvc.perform(put("/progress/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(progressEntryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.itemId").value(1))
                .andExpect(jsonPath("$.itemType").value("SUBJECT"))
                .andExpect(jsonPath("$.itemTitle").value("Test Subject"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.timeSpentMinutes").value(60))
                .andExpect(jsonPath("$.notes").value("Test notes"));

        verify(progressService).updateProgressEntry(eq(1L), any(ProgressEntryRequest.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteProgressEntry_Success() throws Exception {
        // Arrange
        doNothing().when(progressService).deleteProgressEntry(anyLong(), anyString());

        // Act & Assert
        mockMvc.perform(delete("/progress/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(progressService).deleteProgressEntry(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserProgressEntries_Success() throws Exception {
        // Arrange
        when(progressService.getUserProgressEntries(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(progressEntryResponse)));

        // Act & Assert
        mockMvc.perform(get("/progress")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(1))
                .andExpect(jsonPath("$.content[0].itemId").value(1))
                .andExpect(jsonPath("$.content[0].itemType").value("SUBJECT"))
                .andExpect(jsonPath("$.content[0].itemTitle").value("Test Subject"))
                .andExpect(jsonPath("$.content[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.content[0].timeSpentMinutes").value(60))
                .andExpect(jsonPath("$.content[0].notes").value("Test notes"));

        verify(progressService).getUserProgressEntries(eq("testuser"), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserProgressEntriesByDateRange_Success() throws Exception {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        when(progressService.getUserProgressEntriesByDateRange(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(progressEntryResponse));

        // Act & Assert
        mockMvc.perform(get("/progress/date-range")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].itemId").value(1))
                .andExpect(jsonPath("$[0].itemType").value("SUBJECT"))
                .andExpect(jsonPath("$[0].itemTitle").value("Test Subject"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].timeSpentMinutes").value(60))
                .andExpect(jsonPath("$[0].notes").value("Test notes"));

        verify(progressService).getUserProgressEntriesByDateRange(eq("testuser"), eq(startDate), eq(endDate));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getSyllabusCompletionSummary_Success() throws Exception {
        // Arrange
        when(progressService.getSyllabusCompletionSummary(anyLong(), anyString())).thenReturn(completionSummaryResponse);

        // Act & Assert
        mockMvc.perform(get("/progress/analytics/completion/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syllabusId").value(1))
                .andExpect(jsonPath("$.syllabusTitle").value("Test Syllabus"))
                .andExpect(jsonPath("$.overallCompletionPercentage").value(50.0))
                .andExpect(jsonPath("$.subjectCompletions[0].subjectId").value(1))
                .andExpect(jsonPath("$.subjectCompletions[0].subjectTitle").value("Test Subject"))
                .andExpect(jsonPath("$.subjectCompletions[0].completionPercentage").value(50.0))
                .andExpect(jsonPath("$.subjectCompletions[0].completedTopics").value(1))
                .andExpect(jsonPath("$.subjectCompletions[0].totalTopics").value(2));

        verify(progressService).getSyllabusCompletionSummary(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserProgressSummary_Success() throws Exception {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        when(progressService.getUserProgressSummary(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(progressSummaryResponse);

        // Act & Assert
        mockMvc.perform(get("/progress/analytics/summary")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDaysWithProgress").value(3))
                .andExpect(jsonPath("$.totalTimeSpentMinutes").value(180))
                .andExpect(jsonPath("$.streak").value(2))
                .andExpect(jsonPath("$.dailyProgress[0].timeSpentMinutes").value(60))
                .andExpect(jsonPath("$.dailyProgress[0].itemsProgressed").value(1));

        verify(progressService).getUserProgressSummary(eq("testuser"), eq(startDate), eq(endDate));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserTimeDistribution_Success() throws Exception {
        // Arrange
        when(progressService.getUserTimeDistribution(anyString())).thenReturn(timeDistributionResponse);

        // Act & Assert
        mockMvc.perform(get("/progress/analytics/time-distribution")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTimeSpentMinutes").value(180))
                .andExpect(jsonPath("$.subjectDistribution[0].subjectId").value(1))
                .andExpect(jsonPath("$.subjectDistribution[0].subjectTitle").value("Test Subject"))
                .andExpect(jsonPath("$.subjectDistribution[0].timeSpentMinutes").value(180))
                .andExpect(jsonPath("$.subjectDistribution[0].percentageOfTotal").value(100.0));

        verify(progressService).getUserTimeDistribution("testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentStreak_Success() throws Exception {
        // Arrange
        when(progressService.getCurrentStreak(anyString())).thenReturn(2);

        // Act & Assert
        mockMvc.perform(get("/progress/analytics/streak")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.streak").value(2));

        verify(progressService).getCurrentStreak("testuser");
    }
}
