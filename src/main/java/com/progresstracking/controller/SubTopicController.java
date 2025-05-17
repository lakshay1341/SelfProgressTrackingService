package com.progresstracking.controller;

import com.progresstracking.dto.subtopic.SubTopicRequest;
import com.progresstracking.dto.subtopic.SubTopicResponse;
import com.progresstracking.service.SubTopicService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subtopics")
@RequiredArgsConstructor
public class SubTopicController {

    private final SubTopicService subTopicService;

    @PostMapping("/topic/{topicId}")
       public ResponseEntity<SubTopicResponse> createSubTopic(
            @PathVariable Long topicId,
            @Valid @RequestBody SubTopicRequest subTopicRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        SubTopicResponse subTopic = subTopicService.createSubTopic(topicId, subTopicRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(subTopic);
    }

    @GetMapping("/{id}")
       public ResponseEntity<SubTopicResponse> getSubTopic(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        SubTopicResponse subTopic = subTopicService.getSubTopicById(id, userDetails.getUsername());
        return ResponseEntity.ok(subTopic);
    }

    @GetMapping("/topic/{topicId}")
       public ResponseEntity<List<SubTopicResponse>> getSubTopicsByTopic(
            @PathVariable Long topicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<SubTopicResponse> subTopics = subTopicService.getSubTopicsByTopic(topicId, userDetails.getUsername());
        return ResponseEntity.ok(subTopics);
    }

    @PutMapping("/{id}")
       public ResponseEntity<SubTopicResponse> updateSubTopic(
            @PathVariable Long id,
            @Valid @RequestBody SubTopicRequest subTopicRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        SubTopicResponse subTopic = subTopicService.updateSubTopic(id, subTopicRequest, userDetails.getUsername());
        return ResponseEntity.ok(subTopic);
    }

    @DeleteMapping("/{id}")
       public ResponseEntity<Void> deleteSubTopic(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        subTopicService.deleteSubTopic(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/topic/{topicId}/reorder")
       public ResponseEntity<Void> reorderSubTopics(
            @PathVariable Long topicId,
            @RequestBody List<Long> subTopicIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        subTopicService.reorderSubTopics(topicId, subTopicIds, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}

