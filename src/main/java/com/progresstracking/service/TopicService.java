package com.progresstracking.service;

import com.progresstracking.dto.topic.TopicRequest;
import com.progresstracking.dto.topic.TopicResponse;

import java.util.List;

public interface TopicService {

    TopicResponse createTopic(Long subjectId, TopicRequest topicRequest, String username);
    
    TopicResponse getTopicById(Long id, String username);
    
    List<TopicResponse> getTopicsBySubject(Long subjectId, String username);
    
    TopicResponse updateTopic(Long id, TopicRequest topicRequest, String username);
    
    void deleteTopic(Long id, String username);
    
    void reorderTopics(Long subjectId, List<Long> topicIds, String username);
}
