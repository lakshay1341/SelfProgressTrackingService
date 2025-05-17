package com.progresstracking.service;

import com.progresstracking.dto.subtopic.SubTopicRequest;
import com.progresstracking.dto.subtopic.SubTopicResponse;

import java.util.List;

public interface SubTopicService {

    SubTopicResponse createSubTopic(Long topicId, SubTopicRequest subTopicRequest, String username);
    
    SubTopicResponse getSubTopicById(Long id, String username);
    
    List<SubTopicResponse> getSubTopicsByTopic(Long topicId, String username);
    
    SubTopicResponse updateSubTopic(Long id, SubTopicRequest subTopicRequest, String username);
    
    void deleteSubTopic(Long id, String username);
    
    void reorderSubTopics(Long topicId, List<Long> subTopicIds, String username);
}
