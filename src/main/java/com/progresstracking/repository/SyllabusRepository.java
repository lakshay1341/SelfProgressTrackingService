package com.progresstracking.repository;

import com.progresstracking.model.Syllabus;
import com.progresstracking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyllabusRepository extends JpaRepository<Syllabus, Long> {
    
    Page<Syllabus> findByUser(User user, Pageable pageable);
    
    List<Syllabus> findByUser(User user);
    
    @Query("SELECT s FROM Syllabus s WHERE s.isPublic = true")
    Page<Syllabus> findAllPublic(Pageable pageable);
    
    Optional<Syllabus> findByShareableLink(String shareableLink);
    
    @Query("SELECT s FROM Syllabus s WHERE s.user = ?1 OR s.isPublic = true")
    Page<Syllabus> findByUserOrPublic(User user, Pageable pageable);
}
