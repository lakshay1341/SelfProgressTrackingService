package com.progresstracking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "progress_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum ItemType {
        SUBJECT, TOPIC, SUBTOPIC
    }

    public enum Status {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }
}
