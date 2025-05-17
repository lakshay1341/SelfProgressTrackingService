package com.progresstracking.service;

import com.progresstracking.dto.resource.ResourceRequest;
import com.progresstracking.dto.resource.ResourceResponse;
import com.progresstracking.model.Resource;

import java.util.List;

public interface ResourceService {

    ResourceResponse createResource(Long itemId, Resource.ItemType itemType, ResourceRequest resourceRequest, String username);
    
    ResourceResponse getResourceById(Long id, String username);
    
    List<ResourceResponse> getResourcesByItem(Long itemId, Resource.ItemType itemType, String username);
    
    ResourceResponse updateResource(Long id, ResourceRequest resourceRequest, String username);
    
    void deleteResource(Long id, String username);
}
