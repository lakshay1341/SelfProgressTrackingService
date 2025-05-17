package com.progresstracking.controller;

import com.progresstracking.dto.analytics.CompletionSummaryResponse;
import com.progresstracking.dto.analytics.ProgressSummaryResponse;
import com.progresstracking.dto.analytics.TimeDistributionResponse;
import com.progresstracking.dto.progress.ProgressEntryRequest;
import com.progresstracking.dto.progress.ProgressEntryResponse;
import com.progresstracking.service.ProgressService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping
   public ResponseEntity<ProgressEntryResponse> createProgressEntry(
            @Valid @RequestBody ProgressEntryRequest progressEntryRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProgressEntryResponse progressEntry = progressService.createProgressEntry(progressEntryRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(progressEntry);
    }

    @GetMapping("/{id}")
   public ResponseEntity<ProgressEntryResponse> getProgressEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProgressEntryResponse progressEntry = progressService.getProgressEntryById(id, userDetails.getUsername());
        return ResponseEntity.ok(progressEntry);
    }

    @PutMapping("/{id}")
   public ResponseEntity<ProgressEntryResponse> updateProgressEntry(
            @PathVariable Long id,
            @Valid @RequestBody ProgressEntryRequest progressEntryRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProgressEntryResponse progressEntry = progressService.updateProgressEntry(id, progressEntryRequest, userDetails.getUsername());
        return ResponseEntity.ok(progressEntry);
    }

    @DeleteMapping("/{id}")
   public ResponseEntity<Void> deleteProgressEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        progressService.deleteProgressEntry(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
   public ResponseEntity<Page<ProgressEntryResponse>> getUserProgressEntries(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<ProgressEntryResponse> progressEntries = progressService.getUserProgressEntries(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(progressEntries);
    }

    @GetMapping("/date-range")
   public ResponseEntity<List<ProgressEntryResponse>> getUserProgressEntriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ProgressEntryResponse> progressEntries = progressService.getUserProgressEntriesByDateRange(
                userDetails.getUsername(), startDate, endDate);
        return ResponseEntity.ok(progressEntries);
    }

    @GetMapping("/analytics/completion/{syllabusId}")
   public ResponseEntity<CompletionSummaryResponse> getSyllabusCompletionSummary(
            @PathVariable Long syllabusId,
            @AuthenticationPrincipal UserDetails userDetails) {
        CompletionSummaryResponse completionSummary = progressService.getSyllabusCompletionSummary(
                syllabusId, userDetails.getUsername());
        return ResponseEntity.ok(completionSummary);
    }

    @GetMapping("/analytics/summary")
   public ResponseEntity<ProgressSummaryResponse> getUserProgressSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProgressSummaryResponse progressSummary = progressService.getUserProgressSummary(
                userDetails.getUsername(), startDate, endDate);
        return ResponseEntity.ok(progressSummary);
    }

    @GetMapping("/analytics/time-distribution")
   public ResponseEntity<TimeDistributionResponse> getUserTimeDistribution(
            @AuthenticationPrincipal UserDetails userDetails) {
        TimeDistributionResponse timeDistribution = progressService.getUserTimeDistribution(userDetails.getUsername());
        return ResponseEntity.ok(timeDistribution);
    }

    @GetMapping("/analytics/streak")
   public ResponseEntity<Map<String, Integer>> getCurrentStreak(
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer streak = progressService.getCurrentStreak(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("streak", streak));
    }
}

