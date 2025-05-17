package com.progresstracking.service;

import com.progresstracking.dto.syllabus.SyllabusRequest;
import com.progresstracking.dto.syllabus.SyllabusResponse;
import com.progresstracking.exception.ResourceNotFoundException;
import com.progresstracking.exception.UnauthorizedException;
import com.progresstracking.model.Syllabus;
import com.progresstracking.model.User;
import com.progresstracking.repository.SyllabusRepository;
import com.progresstracking.repository.UserRepository;
import com.progresstracking.service.impl.SyllabusServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SyllabusServiceImplTest {

    @Mock
    private SyllabusRepository syllabusRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProgressService progressService;

    @InjectMocks
    private SyllabusServiceImpl syllabusService;

    private User user;
    private User otherUser;
    private Syllabus syllabus;
    private SyllabusRequest syllabusRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .role(User.Role.STUDENT)
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .password("encoded-password")
                .role(User.Role.STUDENT)
                .build();

        syllabus = Syllabus.builder()
                .id(1L)
                .user(user)
                .title("Test Syllabus")
                .description("Test Description")
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        syllabusRequest = SyllabusRequest.builder()
                .title("Test Syllabus")
                .description("Test Description")
                .isPublic(false)
                .build();
    }

    @Test
    void createSyllabus_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(syllabusRepository.save(any(Syllabus.class))).thenReturn(syllabus);
        when(progressService.calculateSyllabusCompletionPercentage(anyLong())).thenReturn(0.0);

        // Act
        SyllabusResponse response = syllabusService.createSyllabus(syllabusRequest, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Syllabus", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertFalse(response.isPublic());
        assertEquals("testuser", response.getUsername());
        assertEquals(0, response.getSubjectCount());
        assertEquals(0.0, response.getCompletionPercentage());

        verify(userRepository).findByUsername("testuser");
        verify(syllabusRepository).save(any(Syllabus.class));
        verify(progressService).calculateSyllabusCompletionPercentage(1L);
    }

    @Test
    void getSyllabusById_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(syllabusRepository.findById(anyLong())).thenReturn(Optional.of(syllabus));
        when(progressService.calculateSyllabusCompletionPercentage(anyLong())).thenReturn(0.0);

        // Act
        SyllabusResponse response = syllabusService.getSyllabusById(1L, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Syllabus", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertFalse(response.isPublic());
        assertEquals("testuser", response.getUsername());
        assertEquals(0, response.getSubjectCount());
        assertEquals(0.0, response.getCompletionPercentage());

        verify(userRepository).findByUsername("testuser");
        verify(syllabusRepository).findById(1L);
        verify(progressService).calculateSyllabusCompletionPercentage(1L);
    }

    @Test
    void getSyllabusById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(syllabusRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> syllabusService.getSyllabusById(1L, "testuser"));
        verify(userRepository).findByUsername("testuser");
        verify(syllabusRepository).findById(1L);
    }

    @Test
    void getSyllabusById_Unauthorized_ThrowsUnauthorizedException() {
        // Arrange
        syllabus.setUser(otherUser);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(syllabusRepository.findById(anyLong())).thenReturn(Optional.of(syllabus));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> syllabusService.getSyllabusById(1L, "testuser"));
        verify(userRepository).findByUsername("testuser");
        verify(syllabusRepository).findById(1L);
    }

    @Test
    void getSyllabusById_PublicSyllabus_Success() {
        // Arrange
        syllabus.setUser(otherUser);
        syllabus.setPublic(true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(syllabusRepository.findById(anyLong())).thenReturn(Optional.of(syllabus));
        when(progressService.calculateSyllabusCompletionPercentage(anyLong())).thenReturn(0.0);

        // Act
        SyllabusResponse response = syllabusService.getSyllabusById(1L, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Syllabus", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertTrue(response.isPublic());
        assertEquals("otheruser", response.getUsername());
        assertEquals(0, response.getSubjectCount());
        assertEquals(0.0, response.getCompletionPercentage());

        verify(userRepository).findByUsername("testuser");
        verify(syllabusRepository).findById(1L);
        verify(progressService).calculateSyllabusCompletionPercentage(1L);
    }

    @Test
    void updateSyllabus_Success() {
        // Arrange
        SyllabusRequest updateRequest = SyllabusRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .isPublic(true)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(syllabusRepository.findById(anyLong())).thenReturn(Optional.of(syllabus));
        when(syllabusRepository.save(any(Syllabus.class))).thenReturn(syllabus);
        when(progressService.calculateSyllabusCompletionPercentage(anyLong())).thenReturn(0.0);

        // Act
        SyllabusResponse response = syllabusService.updateSyllabus(1L, updateRequest, "testuser");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Updated Title", response.getTitle());
        assertEquals("Updated Description", response.getDescription());
        assertTrue(response.isPublic());
        assertEquals("testuser", response.getUsername());
        assertEquals(0, response.getSubjectCount());
        assertEquals(0.0, response.getCompletionPercentage());

        verify(userRepository).findByUsername("testuser");
        verify(syllabusRepository).findById(1L);
        verify(syllabusRepository).save(any(Syllabus.class));
        verify(progressService).calculateSyllabusCompletionPercentage(1L);
    }

    @Test
    void updateSyllabus_Unauthorized_ThrowsUnauthorizedException() {
        // Arrange
        syllabus.setUser(otherUser);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(syllabusRepository.findById(anyLong())).thenReturn(Optional.of(syllabus));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> syllabusService.updateSyllabus(1L, syllabusRequest, "testuser"));
        verify(userRepository).findByUsername("testuser");
        verify(syllabusRepository).findById(1L);
        verify(syllabusRepository, never()).save(any(Syllabus.class));
    }

    @Test
    void deleteSyllabus_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(syllabusRepository.findById(anyLong())).thenReturn(Optional.of(syllabus));
        doNothing().when(syllabusRepository).delete(any(Syllabus.class));

        // Act
        syllabusService.deleteSyllabus(1L, "testuser");

        // Assert
        verify(userRepository).findByUsername("testuser");
        verify(syllabusRepository).findById(1L);
        verify(syllabusRepository).delete(syllabus);
    }

    @Test
    void deleteSyllabus_Unauthorized_ThrowsUnauthorizedException() {
        // Arrange
        syllabus.setUser(otherUser);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(syllabusRepository.findById(anyLong())).thenReturn(Optional.of(syllabus));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> syllabusService.deleteSyllabus(1L, "testuser"));
        verify(userRepository).findByUsername("testuser");
        verify(syllabusRepository).findById(1L);
        verify(syllabusRepository, never()).delete(any(Syllabus.class));
    }

    @Test
    void getUserSyllabi_Success() {
        // Arrange
        Page<Syllabus> syllabusPage = new PageImpl<>(Collections.singletonList(syllabus));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(syllabusRepository.findByUser(any(User.class), any(Pageable.class))).thenReturn(syllabusPage);
        when(progressService.calculateSyllabusCompletionPercentage(anyLong())).thenReturn(0.0);

        // Act
        Page<SyllabusResponse> response = syllabusService.getUserSyllabi("testuser", Pageable.unpaged());

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Test Syllabus", response.getContent().get(0).getTitle());

        verify(userRepository).findByUsername("testuser");
        verify(syllabusRepository).findByUser(user, Pageable.unpaged());
        verify(progressService).calculateSyllabusCompletionPercentage(1L);
    }

    @Test
    void getPublicSyllabi_Success() {
        // Arrange
        Page<Syllabus> syllabusPage = new PageImpl<>(Collections.singletonList(syllabus));
        when(syllabusRepository.findAllPublic(any(Pageable.class))).thenReturn(syllabusPage);
        when(progressService.calculateSyllabusCompletionPercentage(anyLong())).thenReturn(0.0);

        // Act
        Page<SyllabusResponse> response = syllabusService.getPublicSyllabi(Pageable.unpaged());

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Test Syllabus", response.getContent().get(0).getTitle());

        verify(syllabusRepository).findAllPublic(Pageable.unpaged());
        verify(progressService).calculateSyllabusCompletionPercentage(1L);
    }
}
