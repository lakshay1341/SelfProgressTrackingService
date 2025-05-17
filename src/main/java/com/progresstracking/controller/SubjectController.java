package com.progresstracking.controller;

import com.progresstracking.dto.subject.SubjectRequest;
import com.progresstracking.dto.subject.SubjectResponse;
import com.progresstracking.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping("/syllabus/{syllabusId}")
   public ResponseEntity<SubjectResponse> createSubject(
            @PathVariable Long syllabusId,
            @Valid @RequestBody SubjectRequest subjectRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        SubjectResponse subject = subjectService.createSubject(syllabusId, subjectRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(subject);
    }

    @GetMapping("/{id}")
   public ResponseEntity<SubjectResponse> getSubject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        SubjectResponse subject = subjectService.getSubjectById(id, userDetails.getUsername());
        return ResponseEntity.ok(subject);
    }

    @GetMapping("/syllabus/{syllabusId}")
   public ResponseEntity<List<SubjectResponse>> getSubjectsBySyllabus(
            @PathVariable Long syllabusId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<SubjectResponse> subjects = subjectService.getSubjectsBySyllabus(syllabusId, userDetails.getUsername());
        return ResponseEntity.ok(subjects);
    }

    @PutMapping("/{id}")
   public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody SubjectRequest subjectRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        SubjectResponse subject = subjectService.updateSubject(id, subjectRequest, userDetails.getUsername());
        return ResponseEntity.ok(subject);
    }

    @DeleteMapping("/{id}")
   public ResponseEntity<Void> deleteSubject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        subjectService.deleteSubject(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/syllabus/{syllabusId}/reorder")
   public ResponseEntity<Void> reorderSubjects(
            @PathVariable Long syllabusId,
            @RequestBody List<Long> subjectIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        subjectService.reorderSubjects(syllabusId, subjectIds, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}

