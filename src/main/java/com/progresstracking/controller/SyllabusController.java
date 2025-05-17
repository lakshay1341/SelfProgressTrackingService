package com.progresstracking.controller;

import com.progresstracking.dto.syllabus.SyllabusRequest;
import com.progresstracking.dto.syllabus.SyllabusResponse;
import com.progresstracking.service.SyllabusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/syllabi")
@RequiredArgsConstructor
public class SyllabusController {

    private final SyllabusService syllabusService;

    @PostMapping
    public ResponseEntity<SyllabusResponse> createSyllabus(
            @Valid @RequestBody SyllabusRequest syllabusRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        SyllabusResponse syllabus = syllabusService.createSyllabus(syllabusRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(syllabus);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SyllabusResponse> getSyllabus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        SyllabusResponse syllabus = syllabusService.getSyllabusById(id, userDetails.getUsername());
        return ResponseEntity.ok(syllabus);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SyllabusResponse> updateSyllabus(
            @PathVariable Long id,
            @Valid @RequestBody SyllabusRequest syllabusRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        SyllabusResponse syllabus = syllabusService.updateSyllabus(id, syllabusRequest, userDetails.getUsername());
        return ResponseEntity.ok(syllabus);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSyllabus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        syllabusService.deleteSyllabus(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<SyllabusResponse>> getUserSyllabi(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<SyllabusResponse> syllabi = syllabusService.getUserSyllabi(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(syllabi);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<SyllabusResponse>> getPublicSyllabi(Pageable pageable) {
        Page<SyllabusResponse> syllabi = syllabusService.getPublicSyllabi(pageable);
        return ResponseEntity.ok(syllabi);
    }

    @GetMapping("/public/{shareableLink}")
    public ResponseEntity<SyllabusResponse> getSyllabusByShareableLink(@PathVariable String shareableLink) {
        SyllabusResponse syllabus = syllabusService.getSyllabusByShareableLink(shareableLink);
        return ResponseEntity.ok(syllabus);
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<Map<String, String>> generateShareableLink(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String shareableLink = syllabusService.generateShareableLink(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("shareableLink", shareableLink));
    }

    @DeleteMapping("/{id}/share")
    public ResponseEntity<Void> revokeShareableLink(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        syllabusService.revokeShareableLink(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
