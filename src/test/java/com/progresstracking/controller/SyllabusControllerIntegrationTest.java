package com.progresstracking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.progresstracking.dto.syllabus.SyllabusRequest;
import com.progresstracking.dto.syllabus.SyllabusResponse;
import com.progresstracking.service.SyllabusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SyllabusController.class)
public class SyllabusControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SyllabusService syllabusService;

    private SyllabusRequest syllabusRequest;
    private SyllabusResponse syllabusResponse;

    @BeforeEach
    void setUp() {
        syllabusRequest = SyllabusRequest.builder()
                .title("Test Syllabus")
                .description("Test Description")
                .isPublic(false)
                .build();

        syllabusResponse = SyllabusResponse.builder()
                .id(1L)
                .title("Test Syllabus")
                .description("Test Description")
                .isPublic(false)
                .username("testuser")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .subjectCount(0)
                .completionPercentage(0.0)
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void createSyllabus_Success() throws Exception {
        // Arrange
        when(syllabusService.createSyllabus(any(SyllabusRequest.class), anyString())).thenReturn(syllabusResponse);

        // Act & Assert
        mockMvc.perform(post("/syllabi")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(syllabusRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Syllabus"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.public").value(false))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.subjectCount").value(0))
                .andExpect(jsonPath("$.completionPercentage").value(0.0));

        verify(syllabusService).createSyllabus(any(SyllabusRequest.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getSyllabus_Success() throws Exception {
        // Arrange
        when(syllabusService.getSyllabusById(anyLong(), anyString())).thenReturn(syllabusResponse);

        // Act & Assert
        mockMvc.perform(get("/syllabi/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Syllabus"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.public").value(false))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.subjectCount").value(0))
                .andExpect(jsonPath("$.completionPercentage").value(0.0));

        verify(syllabusService).getSyllabusById(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateSyllabus_Success() throws Exception {
        // Arrange
        when(syllabusService.updateSyllabus(anyLong(), any(SyllabusRequest.class), anyString())).thenReturn(syllabusResponse);

        // Act & Assert
        mockMvc.perform(put("/syllabi/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(syllabusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Syllabus"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.public").value(false))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.subjectCount").value(0))
                .andExpect(jsonPath("$.completionPercentage").value(0.0));

        verify(syllabusService).updateSyllabus(eq(1L), any(SyllabusRequest.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteSyllabus_Success() throws Exception {
        // Arrange
        doNothing().when(syllabusService).deleteSyllabus(anyLong(), anyString());

        // Act & Assert
        mockMvc.perform(delete("/syllabi/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(syllabusService).deleteSyllabus(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserSyllabi_Success() throws Exception {
        // Arrange
        when(syllabusService.getUserSyllabi(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(syllabusResponse)));

        // Act & Assert
        mockMvc.perform(get("/syllabi")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Syllabus"))
                .andExpect(jsonPath("$.content[0].description").value("Test Description"))
                .andExpect(jsonPath("$.content[0].public").value(false))
                .andExpect(jsonPath("$.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.content[0].subjectCount").value(0))
                .andExpect(jsonPath("$.content[0].completionPercentage").value(0.0));

        verify(syllabusService).getUserSyllabi(eq("testuser"), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getPublicSyllabi_Success() throws Exception {
        // Arrange
        when(syllabusService.getPublicSyllabi(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(syllabusResponse)));

        // Act & Assert
        mockMvc.perform(get("/syllabi/public")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Syllabus"))
                .andExpect(jsonPath("$.content[0].description").value("Test Description"))
                .andExpect(jsonPath("$.content[0].public").value(false))
                .andExpect(jsonPath("$.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.content[0].subjectCount").value(0))
                .andExpect(jsonPath("$.content[0].completionPercentage").value(0.0));

        verify(syllabusService).getPublicSyllabi(any(Pageable.class));
    }

    @Test
    void getSyllabusByShareableLink_Success() throws Exception {
        // Arrange
        String shareableLink = "shareable-link";
        when(syllabusService.getSyllabusByShareableLink(anyString())).thenReturn(syllabusResponse);

        // Act & Assert
        mockMvc.perform(get("/syllabi/public/" + shareableLink))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Syllabus"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.public").value(false))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.subjectCount").value(0))
                .andExpect(jsonPath("$.completionPercentage").value(0.0));

        verify(syllabusService).getSyllabusByShareableLink(shareableLink);
    }

    @Test
    @WithMockUser(username = "testuser")
    void generateShareableLink_Success() throws Exception {
        // Arrange
        String shareableLink = "shareable-link";
        when(syllabusService.generateShareableLink(anyLong(), anyString())).thenReturn(shareableLink);

        // Act & Assert
        mockMvc.perform(post("/syllabi/1/share")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareableLink").value(shareableLink));

        verify(syllabusService).generateShareableLink(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void revokeShareableLink_Success() throws Exception {
        // Arrange
        doNothing().when(syllabusService).revokeShareableLink(anyLong(), anyString());

        // Act & Assert
        mockMvc.perform(delete("/syllabi/1/share")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(syllabusService).revokeShareableLink(1L, "testuser");
    }
}
