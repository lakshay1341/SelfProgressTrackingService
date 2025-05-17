package com.progresstracking.controller;

import com.progresstracking.dto.resource.ResourceRequest;
import com.progresstracking.dto.resource.ResourceResponse;
import com.progresstracking.model.Resource;
import com.progresstracking.service.ResourceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping("/{itemType}/{itemId}")
       public ResponseEntity<ResourceResponse> createResource(
            @PathVariable Resource.ItemType itemType,
            @PathVariable Long itemId,
            @Valid @RequestBody ResourceRequest resourceRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResourceResponse resource = resourceService.createResource(itemId, itemType, resourceRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @GetMapping("/{id}")
       public ResponseEntity<ResourceResponse> getResource(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResourceResponse resource = resourceService.getResourceById(id, userDetails.getUsername());
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/item/{itemType}/{itemId}")
       public ResponseEntity<List<ResourceResponse>> getResourcesByItem(
            @PathVariable Resource.ItemType itemType,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ResourceResponse> resources = resourceService.getResourcesByItem(itemId, itemType, userDetails.getUsername());
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{id}")
       public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest resourceRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResourceResponse resource = resourceService.updateResource(id, resourceRequest, userDetails.getUsername());
        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{id}")
       public ResponseEntity<Void> deleteResource(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        resourceService.deleteResource(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}

