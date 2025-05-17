package com.progresstracking.dto.resource;

import com.progresstracking.model.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRequest {

    @NotNull(message = "Item type is required")
    private Resource.ItemType itemType;

    @NotNull(message = "Resource type is required")
    private Resource.ResourceType resourceType;

    @NotBlank(message = "Content is required")
    private String content;
}
