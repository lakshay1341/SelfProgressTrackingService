package com.progresstracking.service.impl;

import com.progresstracking.dto.syllabus.SyllabusRequest;
import com.progresstracking.dto.syllabus.SyllabusResponse;
import com.progresstracking.exception.ResourceNotFoundException;
import com.progresstracking.exception.UnauthorizedException;
import com.progresstracking.model.Syllabus;
import com.progresstracking.model.User;
import com.progresstracking.repository.SyllabusRepository;
import com.progresstracking.repository.UserRepository;
import com.progresstracking.service.ProgressService;
import com.progresstracking.service.SyllabusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SyllabusServiceImpl implements SyllabusService {

    private final SyllabusRepository syllabusRepository;
    private final UserRepository userRepository;
    private final ProgressService progressService;

    @Override
    @Transactional
    public SyllabusResponse createSyllabus(SyllabusRequest syllabusRequest, String username) {
        User user = getUserByUsername(username);

        Syllabus syllabus = Syllabus.builder()
                .user(user)
                .title(syllabusRequest.getTitle())
                .description(syllabusRequest.getDescription())
                .isPublic(syllabusRequest.isPublic())
                .build();

        Syllabus savedSyllabus = syllabusRepository.save(syllabus);
        return mapToSyllabusResponse(savedSyllabus);
    }

    @Override
    @Transactional(readOnly = true)
    public SyllabusResponse getSyllabusById(Long id, String username) {
        User user = getUserByUsername(username);
        Syllabus syllabus = getSyllabus(id);

        // Check if user has access to this syllabus
        if (!syllabus.getUser().getId().equals(user.getId()) && !syllabus.isPublic()) {
            throw new UnauthorizedException("You don't have permission to access this syllabus");
        }

        return mapToSyllabusResponse(syllabus);
    }

    @Override
    @Transactional
    public SyllabusResponse updateSyllabus(Long id, SyllabusRequest syllabusRequest, String username) {
        User user = getUserByUsername(username);
        Syllabus syllabus = getSyllabus(id);

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to update this syllabus");
        }

        syllabus.setTitle(syllabusRequest.getTitle());
        syllabus.setDescription(syllabusRequest.getDescription());
        syllabus.setPublic(syllabusRequest.isPublic());

        Syllabus updatedSyllabus = syllabusRepository.save(syllabus);
        return mapToSyllabusResponse(updatedSyllabus);
    }

    @Override
    @Transactional
    public void deleteSyllabus(Long id, String username) {
        User user = getUserByUsername(username);
        Syllabus syllabus = getSyllabus(id);

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this syllabus");
        }

        syllabusRepository.delete(syllabus);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SyllabusResponse> getUserSyllabi(String username, Pageable pageable) {
        User user = getUserByUsername(username);
        Page<Syllabus> syllabi = syllabusRepository.findByUser(user, pageable);
        return syllabi.map(this::mapToSyllabusResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SyllabusResponse> getPublicSyllabi(Pageable pageable) {
        Page<Syllabus> syllabi = syllabusRepository.findAllPublic(pageable);
        return syllabi.map(this::mapToSyllabusResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SyllabusResponse getSyllabusByShareableLink(String shareableLink) {
        Syllabus syllabus = syllabusRepository.findByShareableLink(shareableLink)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "shareableLink", shareableLink));

        return mapToSyllabusResponse(syllabus);
    }

    @Override
    @Transactional
    public String generateShareableLink(Long id, String username) {
        User user = getUserByUsername(username);
        Syllabus syllabus = getSyllabus(id);

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to share this syllabus");
        }

        String shareableLink = UUID.randomUUID().toString();
        syllabus.setShareableLink(shareableLink);
        syllabusRepository.save(syllabus);

        return shareableLink;
    }

    @Override
    @Transactional
    public void revokeShareableLink(Long id, String username) {
        User user = getUserByUsername(username);
        Syllabus syllabus = getSyllabus(id);

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to revoke sharing for this syllabus");
        }

        syllabus.setShareableLink(null);
        syllabusRepository.save(syllabus);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Syllabus getSyllabus(Long id) {
        return syllabusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
    }

    private SyllabusResponse mapToSyllabusResponse(Syllabus syllabus) {
        Double completionPercentage = progressService.calculateSyllabusCompletionPercentage(syllabus.getId());

        // Safely get the subject count, handling null subjects collection
        int subjectCount = syllabus.getSubjects() != null ? syllabus.getSubjects().size() : 0;

        return SyllabusResponse.builder()
                .id(syllabus.getId())
                .title(syllabus.getTitle())
                .description(syllabus.getDescription())
                .isPublic(syllabus.isPublic())
                .shareableLink(syllabus.getShareableLink())
                .username(syllabus.getUser().getUsername())
                .createdAt(syllabus.getCreatedAt())
                .updatedAt(syllabus.getUpdatedAt())
                .subjectCount(subjectCount)
                .completionPercentage(completionPercentage)
                .build();
    }
}
