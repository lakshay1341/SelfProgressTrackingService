package com.progresstracking.service;

import com.progresstracking.dto.analytics.CompletionSummaryResponse;
import com.progresstracking.dto.analytics.ProgressSummaryResponse;
import com.progresstracking.dto.analytics.TimeDistributionResponse;
import com.progresstracking.dto.progress.ProgressEntryRequest;
import com.progresstracking.dto.progress.ProgressEntryResponse;
import com.progresstracking.exception.BadRequestException;
import com.progresstracking.exception.ResourceNotFoundException;
import com.progresstracking.exception.UnauthorizedException;
import com.progresstracking.model.*;
import com.progresstracking.repository.*;
import com.progresstracking.service.impl.ProgressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProgressServiceImplTest {

    @Mock
    private ProgressEntryRepository progressEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SyllabusRepository syllabusRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private SubTopicRepository subTopicRepository;

    @InjectMocks
    private ProgressServiceImpl progressService;

    private User user;
    private Syllabus syllabus;
    private Subject subject;
    private Topic topic;
    private SubTopic subTopic;
    private ProgressEntry progressEntry;
    private ProgressEntryRequest progressEntryRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .role(User.Role.STUDENT)
                .build();

        syllabus = Syllabus.builder()
                .id(1L)
                .user(user)
                .title("Test Syllabus")
                .description("Test Description")
                .isPublic(false)
                .build();

        subject = Subject.builder()
                .id(1L)
                .syllabus(syllabus)
                .title("Test Subject")
                .description("Test Description")
                .order(1)
                .build();

        topic = Topic.builder()
                .id(1L)
                .subject(subject)
                .title("Test Topic")
                .description("Test Description")
                .order(1)
                .build();

        subTopic = SubTopic.builder()
                .id(1L)
                .topic(topic)
                .title("Test SubTopic")
                .description("Test Description")
                .order(1)
                .build();

        progressEntry = ProgressEntry.builder()
                .id(1L)
                .user(user)
                .itemId(1L)
                .itemType(ProgressEntry.ItemType.SUBJECT)
                .date(LocalDate.now())
                .status(ProgressEntry.Status.IN_PROGRESS)
                .timeSpentMinutes(60)
                .notes("Test notes")
                .build();

        progressEntryRequest = ProgressEntryRequest.builder()
                .itemId(1L)
                .itemType(ProgressEntry.ItemType.SUBJECT)
                .date(LocalDate.now())
                .status(ProgressEntry.Status.IN_PROGRESS)
                .timeSpentMinutes(60)
                .notes("Test notes")
                .build();
    }

    @Test
    void createProgressEntry_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(subjectRepository.findById(anyLong())).thenReturn(Optional.of(subject));
        when(progressEntryRepository.findByUserAndItemTypeAndItemIdAndDate(any(), any(), anyLong(), any()))
                .thenReturn(Optional.empty());
        when(progressEntryRepository.save(any(ProgressEntry.class))).thenReturn(progressEntry);

        // Act
        ProgressEntryResponse response = progressService.createProgressEntry(progressEntryRequest, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(1L, response.getItemId());
        assertEquals(ProgressEntry.ItemType.SUBJECT, response.getItemType());
        assertEquals("Test Subject", response.getItemTitle());
        assertEquals(LocalDate.now(), response.getDate());
        assertEquals(ProgressEntry.Status.IN_PROGRESS, response.getStatus());
        assertEquals(60, response.getTimeSpentMinutes());
        assertEquals("Test notes", response.getNotes());

        verify(userRepository).findByUsername("testuser");
        verify(subjectRepository).findById(1L);
        verify(progressEntryRepository).findByUserAndItemTypeAndItemIdAndDate(
                user, ProgressEntry.ItemType.SUBJECT, 1L, LocalDate.now());
        verify(progressEntryRepository).save(any(ProgressEntry.class));
    }

    @Test
    void createProgressEntry_EntryExists_ThrowsBadRequestException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(subjectRepository.findById(anyLong())).thenReturn(Optional.of(subject));
        when(progressEntryRepository.findByUserAndItemTypeAndItemIdAndDate(any(), any(), anyLong(), any()))
                .thenReturn(Optional.of(progressEntry));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> progressService.createProgressEntry(progressEntryRequest, "testuser"));
        verify(userRepository).findByUsername("testuser");
        verify(subjectRepository).findById(1L);
        verify(progressEntryRepository).findByUserAndItemTypeAndItemIdAndDate(
                user, ProgressEntry.ItemType.SUBJECT, 1L, LocalDate.now());
        verify(progressEntryRepository, never()).save(any(ProgressEntry.class));
    }

    @Test
    void getProgressEntryById_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(progressEntryRepository.findById(anyLong())).thenReturn(Optional.of(progressEntry));
        when(subjectRepository.findById(anyLong())).thenReturn(Optional.of(subject));

        // Act
        ProgressEntryResponse response = progressService.getProgressEntryById(1L, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(1L, response.getItemId());
        assertEquals(ProgressEntry.ItemType.SUBJECT, response.getItemType());
        assertEquals("Test Subject", response.getItemTitle());
        assertEquals(LocalDate.now(), response.getDate());
        assertEquals(ProgressEntry.Status.IN_PROGRESS, response.getStatus());
        assertEquals(60, response.getTimeSpentMinutes());
        assertEquals("Test notes", response.getNotes());

        verify(userRepository).findByUsername("testuser");
        verify(progressEntryRepository).findById(1L);
        verify(subjectRepository).findById(1L);
    }

    @Test
    void getProgressEntryById_Unauthorized_ThrowsUnauthorizedException() {
        // Arrange
        User otherUser = User.builder().id(2L).username("otheruser").build();
        progressEntry.setUser(otherUser);
        
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(progressEntryRepository.findById(anyLong())).thenReturn(Optional.of(progressEntry));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> progressService.getProgressEntryById(1L, "testuser"));
        verify(userRepository).findByUsername("testuser");
        verify(progressEntryRepository).findById(1L);
    }

    @Test
    void calculateSyllabusCompletionPercentage_Success() {
        // Arrange
        when(syllabusRepository.findById(anyLong())).thenReturn(Optional.of(syllabus));
        when(subjectRepository.findBySyllabusOrderByOrder(any(Syllabus.class)))
                .thenReturn(Collections.singletonList(subject));
        when(topicRepository.findBySubjectOrderByOrder(any(Subject.class)))
                .thenReturn(Collections.singletonList(topic));
        when(subTopicRepository.findByTopicOrderByOrder(any(Topic.class)))
                .thenReturn(Collections.singletonList(subTopic));
        when(progressEntryRepository.findByUserAndItemTypeAndItemId(any(User.class), any(), anyLong()))
                .thenReturn(Collections.singletonList(progressEntry));

        // Act
        Double percentage = progressService.calculateSyllabusCompletionPercentage(1L);

        // Assert
        assertNotNull(percentage);
        assertEquals(50.0, percentage);

        verify(syllabusRepository).findById(1L);
        verify(subjectRepository).findBySyllabusOrderByOrder(syllabus);
    }

    @Test
    void getUserProgressSummary_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(progressEntryRepository.findByUserAndDateBetween(any(User.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(progressEntry));
        when(progressEntryRepository.countDistinctDatesByUserAndDateBetween(any(User.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(1L);
        when(progressEntryRepository.sumTimeSpentByUserAndDateBetween(any(User.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(60);
        when(progressEntryRepository.findDistinctDatesByUserOrderByDateDesc(any(User.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(LocalDate.now()));

        // Act
        ProgressSummaryResponse response = progressService.getUserProgressSummary("testuser", startDate, endDate);

        // Assert
        assertNotNull(response);
        assertEquals(startDate, response.getStartDate());
        assertEquals(endDate, response.getEndDate());
        assertEquals(1, response.getTotalDaysWithProgress());
        assertEquals(60, response.getTotalTimeSpentMinutes());
        assertEquals(1, response.getStreak());
        assertEquals(1, response.getDailyProgress().size());
        assertEquals(LocalDate.now(), response.getDailyProgress().get(0).getDate());
        assertEquals(60, response.getDailyProgress().get(0).getTimeSpentMinutes());
        assertEquals(1, response.getDailyProgress().get(0).getItemsProgressed());

        verify(userRepository).findByUsername("testuser");
        verify(progressEntryRepository).findByUserAndDateBetween(user, startDate, endDate);
        verify(progressEntryRepository).countDistinctDatesByUserAndDateBetween(user, startDate, endDate);
        verify(progressEntryRepository).sumTimeSpentByUserAndDateBetween(user, startDate, endDate);
    }

    @Test
    void getUserTimeDistribution_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(progressEntryRepository.findByUser(any(User.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(progressEntry)));
        when(subjectRepository.findById(anyLong())).thenReturn(Optional.of(subject));

        // Act
        TimeDistributionResponse response = progressService.getUserTimeDistribution("testuser");

        // Assert
        assertNotNull(response);
        assertEquals(60, response.getTotalTimeSpentMinutes());
        assertEquals(1, response.getSubjectDistribution().size());
        assertEquals(1L, response.getSubjectDistribution().get(0).getSubjectId());
        assertEquals("Test Subject", response.getSubjectDistribution().get(0).getSubjectTitle());
        assertEquals(60, response.getSubjectDistribution().get(0).getTimeSpentMinutes());
        assertEquals(100.0, response.getSubjectDistribution().get(0).getPercentageOfTotal());

        verify(userRepository).findByUsername("testuser");
        verify(progressEntryRepository).findByUser(user, Pageable.unpaged());
        verify(subjectRepository).findById(1L);
    }

    @Test
    void getCurrentStreak_Success() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(progressEntryRepository.findDistinctDatesByUserOrderByDateDesc(any(User.class), any(Pageable.class)))
                .thenReturn(Arrays.asList(today, yesterday));

        // Act
        Integer streak = progressService.getCurrentStreak("testuser");

        // Assert
        assertEquals(2, streak);
        verify(userRepository).findByUsername("testuser");
        verify(progressEntryRepository).findDistinctDatesByUserOrderByDateDesc(user, Pageable.unpaged());
    }

    @Test
    void getCurrentStreak_NoEntries_ReturnsZero() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(progressEntryRepository.findDistinctDatesByUserOrderByDateDesc(any(User.class), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        // Act
        Integer streak = progressService.getCurrentStreak("testuser");

        // Assert
        assertEquals(0, streak);
        verify(userRepository).findByUsername("testuser");
        verify(progressEntryRepository).findDistinctDatesByUserOrderByDateDesc(user, Pageable.unpaged());
    }
}
