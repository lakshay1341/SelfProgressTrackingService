package com.progresstracking.repository;

import com.progresstracking.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findBySubjectId(Long subjectId);

    List<Resource> findByTopicId(Long topicId);

    List<Resource> findBySubTopicId(Long subTopicId);

    @Query("SELECT r FROM Resource r WHERE " +
           "(r.itemType = :itemType AND " +
           "(:itemType = com.progresstracking.model.Resource$ItemType.SUBJECT AND r.subject.id = :itemId) OR " +
           "(:itemType = com.progresstracking.model.Resource$ItemType.TOPIC AND r.topic.id = :itemId) OR " +
           "(:itemType = com.progresstracking.model.Resource$ItemType.SUBTOPIC AND r.subTopic.id = :itemId))")
    List<Resource> findByItemTypeAndItemId(@Param("itemType") Resource.ItemType itemType, @Param("itemId") Long itemId);

    void deleteBySubjectId(Long subjectId);

    void deleteByTopicId(Long topicId);

    void deleteBySubTopicId(Long subTopicId);
}
