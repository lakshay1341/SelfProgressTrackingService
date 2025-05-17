package com.progresstracking.repository;

import com.progresstracking.model.Subject;
import com.progresstracking.model.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    
    List<Subject> findBySyllabusOrderByOrder(Syllabus syllabus);
    
    @Query("SELECT MAX(s.order) FROM Subject s WHERE s.syllabus.id = ?1")
    Integer findMaxOrderBySyllabusId(Long syllabusId);
}
