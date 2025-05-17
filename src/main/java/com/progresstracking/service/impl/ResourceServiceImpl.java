package com.progresstracking.service.impl;

import com.progresstracking.dto.resource.ResourceRequest;
import com.progresstracking.dto.resource.ResourceResponse;
import com.progresstracking.exception.BadRequestException;
import com.progresstracking.exception.ResourceNotFoundException;
import com.progresstracking.exception.UnauthorizedException;
import com.progresstracking.model.*;
import com.progresstracking.repository.*;
import com.progresstracking.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final SubTopicRepository subTopicRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ResourceResponse createResource(Long itemId, Resource.ItemType itemType, ResourceRequest resourceRequest, String username) {
        User user = getUserByUsername(username);
        Syllabus syllabus = getSyllabusByItemTypeAndId(itemType, itemId);

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to add resources to this item");
        }

        Resource resource = Resource.builder()
                .itemType(itemType)
                .resourceType(resourceRequest.getResourceType())
                .content(resourceRequest.getContent())
                .build();

        // Set the appropriate parent entity based on item type
        switch (itemType) {
            case SUBJECT:
                Subject subject = getSubject(itemId);
                resource.setSubject(subject);
                break;
            case TOPIC:
                Topic topic = getTopic(itemId);
                resource.setTopic(topic);
                break;
            case SUBTOPIC:
                SubTopic subTopic = getSubTopic(itemId);
                resource.setSubTopic(subTopic);
                break;
            default:
                throw new BadRequestException("Invalid item type");
        }

        Resource savedResource = resourceRepository.save(resource);
        return mapToResourceResponse(savedResource);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id, String username) {
        User user = getUserByUsername(username);
        Resource resource = getResource(id);
        Syllabus syllabus = getSyllabusByResource(resource);

        // Check if user has access to this syllabus
        if (!syllabus.getUser().getId().equals(user.getId()) && !syllabus.isPublic()) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }

        return mapToResourceResponse(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getResourcesByItem(Long itemId, Resource.ItemType itemType, String username) {
        User user = getUserByUsername(username);
        Syllabus syllabus = getSyllabusByItemTypeAndId(itemType, itemId);

        // Check if user has access to this syllabus
        if (!syllabus.getUser().getId().equals(user.getId()) && !syllabus.isPublic()) {
            throw new UnauthorizedException("You don't have permission to access resources for this item");
        }

        List<Resource> resources = resourceRepository.findByItemTypeAndItemId(itemType, itemId);
        return resources.stream()
                .map(this::mapToResourceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResourceResponse updateResource(Long id, ResourceRequest resourceRequest, String username) {
        User user = getUserByUsername(username);
        Resource resource = getResource(id);
        Syllabus syllabus = getSyllabusByResource(resource);

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to update this resource");
        }

        resource.setResourceType(resourceRequest.getResourceType());
        resource.setContent(resourceRequest.getContent());

        Resource updatedResource = resourceRepository.save(resource);
        return mapToResourceResponse(updatedResource);
    }

    @Override
    @Transactional
    public void deleteResource(Long id, String username) {
        User user = getUserByUsername(username);
        Resource resource = getResource(id);
        Syllabus syllabus = getSyllabusByResource(resource);

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this resource");
        }

        resourceRepository.delete(resource);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Subject getSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
    }

    private Topic getTopic(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", id));
    }

    private SubTopic getSubTopic(Long id) {
        return subTopicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubTopic", "id", id));
    }

    private Resource getResource(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));
    }

    private Syllabus getSyllabusByItemTypeAndId(Resource.ItemType itemType, Long itemId) {
        switch (itemType) {
            case SUBJECT:
                Subject subject = getSubject(itemId);
                return subject.getSyllabus();
            case TOPIC:
                Topic topic = getTopic(itemId);
                return topic.getSubject().getSyllabus();
            case SUBTOPIC:
                SubTopic subTopic = getSubTopic(itemId);
                return subTopic.getTopic().getSubject().getSyllabus();
            default:
                throw new BadRequestException("Invalid item type");
        }
    }

    private Syllabus getSyllabusByResource(Resource resource) {
        if (resource.getSubject() != null) {
            return resource.getSubject().getSyllabus();
        } else if (resource.getTopic() != null) {
            return resource.getTopic().getSubject().getSyllabus();
        } else if (resource.getSubTopic() != null) {
            return resource.getSubTopic().getTopic().getSubject().getSyllabus();
        } else {
            throw new BadRequestException("Resource is not associated with any syllabus item");
        }
    }

    private ResourceResponse mapToResourceResponse(Resource resource) {
        Long itemId;
        if (resource.getSubject() != null) {
            itemId = resource.getSubject().getId();
        } else if (resource.getTopic() != null) {
            itemId = resource.getTopic().getId();
        } else {
            itemId = resource.getSubTopic().getId();
        }

        return ResourceResponse.builder()
                .id(resource.getId())
                .itemId(itemId)
                .itemType(resource.getItemType())
                .resourceType(resource.getResourceType())
                .content(resource.getContent())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
