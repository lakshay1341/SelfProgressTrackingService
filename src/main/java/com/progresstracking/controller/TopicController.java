package com.progresstracking.controller;

import com.progresstracking.dto.topic.TopicRequest;
import com.progresstracking.dto.topic.TopicResponse;
import com.progresstracking.service.TopicService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @PostMapping("/subject/{subjectId}")
       public ResponseEntity<TopicResponse> createTopic(
            @PathVariable Long subjectId,
            @Valid @RequestBody TopicRequest topicRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        TopicResponse topic = topicService.createTopic(subjectId, topicRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(topic);
    }

    @GetMapping("/{id}")
       public ResponseEntity<TopicResponse> getTopic(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        TopicResponse topic = topicService.getTopicById(id, userDetails.getUsername());
        return ResponseEntity.ok(topic);
    }

    @GetMapping("/subject/{subjectId}")
       public ResponseEntity<List<TopicResponse>> getTopicsBySubject(
            @PathVariable Long subjectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TopicResponse> topics = topicService.getTopicsBySubject(subjectId, userDetails.getUsername());
        return ResponseEntity.ok(topics);
    }

    @PutMapping("/{id}")
       public ResponseEntity<TopicResponse> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody TopicRequest topicRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        TopicResponse topic = topicService.updateTopic(id, topicRequest, userDetails.getUsername());
        return ResponseEntity.ok(topic);
    }

    @DeleteMapping("/{id}")
       public ResponseEntity<Void> deleteTopic(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        topicService.deleteTopic(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/subject/{subjectId}/reorder")
       public ResponseEntity<Void> reorderTopics(
            @PathVariable Long subjectId,
            @RequestBody List<Long> topicIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        topicService.reorderTopics(subjectId, topicIds, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}

