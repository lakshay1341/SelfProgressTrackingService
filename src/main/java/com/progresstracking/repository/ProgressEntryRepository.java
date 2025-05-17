package com.progresstracking.repository;

import com.progresstracking.model.ProgressEntry;
import com.progresstracking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressEntryRepository extends JpaRepository<ProgressEntry, Long> {
    
    List<ProgressEntry> findByUserAndItemTypeAndItemId(User user, ProgressEntry.ItemType itemType, Long itemId);
    
    Page<ProgressEntry> findByUser(User user, Pageable pageable);
    
    List<ProgressEntry> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    Optional<ProgressEntry> findByUserAndItemTypeAndItemIdAndDate(User user, ProgressEntry.ItemType itemType, Long itemId, LocalDate date);
    
    @Query("SELECT COUNT(DISTINCT p.date) FROM ProgressEntry p WHERE p.user = ?1 AND p.date BETWEEN ?2 AND ?3")
    Long countDistinctDatesByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT p.date FROM ProgressEntry p WHERE p.user = ?1 GROUP BY p.date ORDER BY p.date DESC")
    List<LocalDate> findDistinctDatesByUserOrderByDateDesc(User user, Pageable pageable);
    
    @Query("SELECT SUM(p.timeSpentMinutes) FROM ProgressEntry p WHERE p.user = ?1 AND p.date BETWEEN ?2 AND ?3")
    Integer sumTimeSpentByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT p.itemType, SUM(p.timeSpentMinutes) FROM ProgressEntry p WHERE p.user = ?1 GROUP BY p.itemType")
    List<Object[]> sumTimeSpentByUserGroupByItemType(User user);
}
