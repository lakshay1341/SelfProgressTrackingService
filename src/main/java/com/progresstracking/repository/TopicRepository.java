package com.progresstracking.repository;

import com.progresstracking.model.Subject;
import com.progresstracking.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    
    List<Topic> findBySubjectOrderByOrder(Subject subject);
    
    @Query("SELECT MAX(t.order) FROM Topic t WHERE t.subject.id = ?1")
    Integer findMaxOrderBySubjectId(Long subjectId);
}
