package com.progresstracking.dto.resource;

import com.progresstracking.model.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {

    private Long id;
    private Long itemId;
    private Resource.ItemType itemType;
    private Resource.ResourceType resourceType;
    private String content;
    private LocalDateTime createdAt;
}
