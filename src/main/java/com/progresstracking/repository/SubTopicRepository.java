package com.progresstracking.repository;

import com.progresstracking.model.SubTopic;
import com.progresstracking.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTopicRepository extends JpaRepository<SubTopic, Long> {
    
    List<SubTopic> findByTopicOrderByOrder(Topic topic);
    
    @Query("SELECT MAX(st.order) FROM SubTopic st WHERE st.topic.id = ?1")
    Integer findMaxOrderByTopicId(Long topicId);
}
