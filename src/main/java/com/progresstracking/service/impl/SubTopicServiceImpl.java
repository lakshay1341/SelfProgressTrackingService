package com.progresstracking.service.impl;

import com.progresstracking.dto.subtopic.SubTopicRequest;
import com.progresstracking.dto.subtopic.SubTopicResponse;
import com.progresstracking.exception.BadRequestException;
import com.progresstracking.exception.ResourceNotFoundException;
import com.progresstracking.exception.UnauthorizedException;
import com.progresstracking.model.SubTopic;
import com.progresstracking.model.Syllabus;
import com.progresstracking.model.Topic;
import com.progresstracking.model.User;
import com.progresstracking.repository.SubTopicRepository;
import com.progresstracking.repository.TopicRepository;
import com.progresstracking.repository.UserRepository;
import com.progresstracking.service.ProgressService;
import com.progresstracking.service.SubTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubTopicServiceImpl implements SubTopicService {

    private final SubTopicRepository subTopicRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final ProgressService progressService;

    @Override
    @Transactional
    public SubTopicResponse createSubTopic(Long topicId, SubTopicRequest subTopicRequest, String username) {
        User user = getUserByUsername(username);
        Topic topic = getTopic(topicId);
        Syllabus syllabus = topic.getSubject().getSyllabus();

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to add subtopics to this topic");
        }

        // If order is not provided, place at the end
        Integer order = subTopicRequest.getOrder();
        if (order == null) {
            Integer maxOrder = subTopicRepository.findMaxOrderByTopicId(topicId);
            order = (maxOrder == null) ? 1 : maxOrder + 1;
        }

        SubTopic subTopic = SubTopic.builder()
                .topic(topic)
                .title(subTopicRequest.getTitle())
                .description(subTopicRequest.getDescription())
                .order(order)
                .targetCompletionDate(subTopicRequest.getTargetCompletionDate())
                .build();

        SubTopic savedSubTopic = subTopicRepository.save(subTopic);
        return mapToSubTopicResponse(savedSubTopic);
    }

    @Override
    @Transactional(readOnly = true)
    public SubTopicResponse getSubTopicById(Long id, String username) {
        User user = getUserByUsername(username);
        SubTopic subTopic = getSubTopic(id);
        Syllabus syllabus = subTopic.getTopic().getSubject().getSyllabus();

        // Check if user has access to this syllabus
        if (!syllabus.getUser().getId().equals(user.getId()) && !syllabus.isPublic()) {
            throw new UnauthorizedException("You don't have permission to access this subtopic");
        }

        return mapToSubTopicResponse(subTopic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubTopicResponse> getSubTopicsByTopic(Long topicId, String username) {
        User user = getUserByUsername(username);
        Topic topic = getTopic(topicId);
        Syllabus syllabus = topic.getSubject().getSyllabus();

        // Check if user has access to this syllabus
        if (!syllabus.getUser().getId().equals(user.getId()) && !syllabus.isPublic()) {
            throw new UnauthorizedException("You don't have permission to access subtopics in this topic");
        }

        List<SubTopic> subTopics = subTopicRepository.findByTopicOrderByOrder(topic);
        return subTopics.stream()
                .map(this::mapToSubTopicResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubTopicResponse updateSubTopic(Long id, SubTopicRequest subTopicRequest, String username) {
        User user = getUserByUsername(username);
        SubTopic subTopic = getSubTopic(id);
        Syllabus syllabus = subTopic.getTopic().getSubject().getSyllabus();

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to update this subtopic");
        }

        subTopic.setTitle(subTopicRequest.getTitle());
        subTopic.setDescription(subTopicRequest.getDescription());
        
        // Only update order if provided
        if (subTopicRequest.getOrder() != null) {
            subTopic.setOrder(subTopicRequest.getOrder());
        }
        
        subTopic.setTargetCompletionDate(subTopicRequest.getTargetCompletionDate());

        SubTopic updatedSubTopic = subTopicRepository.save(subTopic);
        return mapToSubTopicResponse(updatedSubTopic);
    }

    @Override
    @Transactional
    public void deleteSubTopic(Long id, String username) {
        User user = getUserByUsername(username);
        SubTopic subTopic = getSubTopic(id);
        Syllabus syllabus = subTopic.getTopic().getSubject().getSyllabus();

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this subtopic");
        }

        subTopicRepository.delete(subTopic);
    }

    @Override
    @Transactional
    public void reorderSubTopics(Long topicId, List<Long> subTopicIds, String username) {
        User user = getUserByUsername(username);
        Topic topic = getTopic(topicId);
        Syllabus syllabus = topic.getSubject().getSyllabus();

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to reorder subtopics in this topic");
        }

        // Check if all subtopic IDs belong to this topic
        List<SubTopic> subTopics = subTopicRepository.findByTopicOrderByOrder(topic);
        List<Long> existingIds = subTopics.stream().map(SubTopic::getId).collect(Collectors.toList());

        if (!existingIds.containsAll(subTopicIds) || existingIds.size() != subTopicIds.size()) {
            throw new BadRequestException("Invalid subtopic IDs provided for reordering");
        }

        // Update order for each subtopic
        for (int i = 0; i < subTopicIds.size(); i++) {
            Long subTopicId = subTopicIds.get(i);
            SubTopic subTopic = subTopics.stream()
                    .filter(st -> st.getId().equals(subTopicId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("SubTopic", "id", subTopicId));
            
            subTopic.setOrder(i + 1);
            subTopicRepository.save(subTopic);
        }
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Topic getTopic(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", id));
    }

    private SubTopic getSubTopic(Long id) {
        return subTopicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubTopic", "id", id));
    }

    private SubTopicResponse mapToSubTopicResponse(SubTopic subTopic) {
        Double completionPercentage = progressService.calculateSubTopicCompletionPercentage(subTopic.getId());
        
        return SubTopicResponse.builder()
                .id(subTopic.getId())
                .topicId(subTopic.getTopic().getId())
                .title(subTopic.getTitle())
                .description(subTopic.getDescription())
                .order(subTopic.getOrder())
                .targetCompletionDate(subTopic.getTargetCompletionDate())
                .completionPercentage(completionPercentage)
                .build();
    }
}
