package com.progresstracking.service.impl;

import com.progresstracking.dto.topic.TopicRequest;
import com.progresstracking.dto.topic.TopicResponse;
import com.progresstracking.exception.BadRequestException;
import com.progresstracking.exception.ResourceNotFoundException;
import com.progresstracking.exception.UnauthorizedException;
import com.progresstracking.model.Subject;
import com.progresstracking.model.Syllabus;
import com.progresstracking.model.Topic;
import com.progresstracking.model.User;
import com.progresstracking.repository.SubjectRepository;
import com.progresstracking.repository.TopicRepository;
import com.progresstracking.repository.UserRepository;
import com.progresstracking.service.ProgressService;
import com.progresstracking.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ProgressService progressService;

    @Override
    @Transactional
    public TopicResponse createTopic(Long subjectId, TopicRequest topicRequest, String username) {
        User user = getUserByUsername(username);
        Subject subject = getSubject(subjectId);
        Syllabus syllabus = subject.getSyllabus();

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to add topics to this subject");
        }

        // If order is not provided, place at the end
        Integer order = topicRequest.getOrder();
        if (order == null) {
            Integer maxOrder = topicRepository.findMaxOrderBySubjectId(subjectId);
            order = (maxOrder == null) ? 1 : maxOrder + 1;
        }

        Topic topic = Topic.builder()
                .subject(subject)
                .title(topicRequest.getTitle())
                .description(topicRequest.getDescription())
                .order(order)
                .targetCompletionDate(topicRequest.getTargetCompletionDate())
                .build();

        Topic savedTopic = topicRepository.save(topic);
        return mapToTopicResponse(savedTopic);
    }

    @Override
    @Transactional(readOnly = true)
    public TopicResponse getTopicById(Long id, String username) {
        User user = getUserByUsername(username);
        Topic topic = getTopic(id);
        Syllabus syllabus = topic.getSubject().getSyllabus();

        // Check if user has access to this syllabus
        if (!syllabus.getUser().getId().equals(user.getId()) && !syllabus.isPublic()) {
            throw new UnauthorizedException("You don't have permission to access this topic");
        }

        return mapToTopicResponse(topic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicResponse> getTopicsBySubject(Long subjectId, String username) {
        User user = getUserByUsername(username);
        Subject subject = getSubject(subjectId);
        Syllabus syllabus = subject.getSyllabus();

        // Check if user has access to this syllabus
        if (!syllabus.getUser().getId().equals(user.getId()) && !syllabus.isPublic()) {
            throw new UnauthorizedException("You don't have permission to access topics in this subject");
        }

        List<Topic> topics = topicRepository.findBySubjectOrderByOrder(subject);
        return topics.stream()
                .map(this::mapToTopicResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TopicResponse updateTopic(Long id, TopicRequest topicRequest, String username) {
        User user = getUserByUsername(username);
        Topic topic = getTopic(id);
        Syllabus syllabus = topic.getSubject().getSyllabus();

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to update this topic");
        }

        topic.setTitle(topicRequest.getTitle());
        topic.setDescription(topicRequest.getDescription());
        
        // Only update order if provided
        if (topicRequest.getOrder() != null) {
            topic.setOrder(topicRequest.getOrder());
        }
        
        topic.setTargetCompletionDate(topicRequest.getTargetCompletionDate());

        Topic updatedTopic = topicRepository.save(topic);
        return mapToTopicResponse(updatedTopic);
    }

    @Override
    @Transactional
    public void deleteTopic(Long id, String username) {
        User user = getUserByUsername(username);
        Topic topic = getTopic(id);
        Syllabus syllabus = topic.getSubject().getSyllabus();

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this topic");
        }

        topicRepository.delete(topic);
    }

    @Override
    @Transactional
    public void reorderTopics(Long subjectId, List<Long> topicIds, String username) {
        User user = getUserByUsername(username);
        Subject subject = getSubject(subjectId);
        Syllabus syllabus = subject.getSyllabus();

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to reorder topics in this subject");
        }

        // Check if all topic IDs belong to this subject
        List<Topic> topics = topicRepository.findBySubjectOrderByOrder(subject);
        List<Long> existingIds = topics.stream().map(Topic::getId).collect(Collectors.toList());

        if (!existingIds.containsAll(topicIds) || existingIds.size() != topicIds.size()) {
            throw new BadRequestException("Invalid topic IDs provided for reordering");
        }

        // Update order for each topic
        for (int i = 0; i < topicIds.size(); i++) {
            Long topicId = topicIds.get(i);
            Topic topic = topics.stream()
                    .filter(t -> t.getId().equals(topicId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", topicId));
            
            topic.setOrder(i + 1);
            topicRepository.save(topic);
        }
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

    private TopicResponse mapToTopicResponse(Topic topic) {
        Double completionPercentage = progressService.calculateTopicCompletionPercentage(topic.getId());
        
        return TopicResponse.builder()
                .id(topic.getId())
                .subjectId(topic.getSubject().getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .order(topic.getOrder())
                .targetCompletionDate(topic.getTargetCompletionDate())
                .subTopicCount(topic.getSubTopics().size())
                .completionPercentage(completionPercentage)
                .build();
    }
}
