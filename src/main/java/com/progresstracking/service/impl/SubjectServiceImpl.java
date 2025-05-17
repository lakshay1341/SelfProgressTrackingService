package com.progresstracking.service.impl;

import com.progresstracking.dto.subject.SubjectRequest;
import com.progresstracking.dto.subject.SubjectResponse;
import com.progresstracking.exception.BadRequestException;
import com.progresstracking.exception.ResourceNotFoundException;
import com.progresstracking.exception.UnauthorizedException;
import com.progresstracking.model.Subject;
import com.progresstracking.model.Syllabus;
import com.progresstracking.model.User;
import com.progresstracking.repository.SubjectRepository;
import com.progresstracking.repository.SyllabusRepository;
import com.progresstracking.repository.UserRepository;
import com.progresstracking.service.ProgressService;
import com.progresstracking.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SyllabusRepository syllabusRepository;
    private final UserRepository userRepository;
    private final ProgressService progressService;

    @Override
    @Transactional
    public SubjectResponse createSubject(Long syllabusId, SubjectRequest subjectRequest, String username) {
        User user = getUserByUsername(username);
        Syllabus syllabus = getSyllabus(syllabusId);

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to add subjects to this syllabus");
        }

        // If order is not provided, place at the end
        Integer order = subjectRequest.getOrder();
        if (order == null) {
            Integer maxOrder = subjectRepository.findMaxOrderBySyllabusId(syllabusId);
            order = (maxOrder == null) ? 1 : maxOrder + 1;
        }

        Subject subject = Subject.builder()
                .syllabus(syllabus)
                .title(subjectRequest.getTitle())
                .description(subjectRequest.getDescription())
                .order(order)
                .targetCompletionDate(subjectRequest.getTargetCompletionDate())
                .build();

        Subject savedSubject = subjectRepository.save(subject);
        return mapToSubjectResponse(savedSubject);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id, String username) {
        User user = getUserByUsername(username);
        Subject subject = getSubject(id);
        Syllabus syllabus = subject.getSyllabus();

        // Check if user has access to this syllabus
        if (!syllabus.getUser().getId().equals(user.getId()) && !syllabus.isPublic()) {
            throw new UnauthorizedException("You don't have permission to access this subject");
        }

        return mapToSubjectResponse(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getSubjectsBySyllabus(Long syllabusId, String username) {
        User user = getUserByUsername(username);
        Syllabus syllabus = getSyllabus(syllabusId);

        // Check if user has access to this syllabus
        if (!syllabus.getUser().getId().equals(user.getId()) && !syllabus.isPublic()) {
            throw new UnauthorizedException("You don't have permission to access subjects in this syllabus");
        }

        List<Subject> subjects = subjectRepository.findBySyllabusOrderByOrder(syllabus);
        return subjects.stream()
                .map(this::mapToSubjectResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubjectResponse updateSubject(Long id, SubjectRequest subjectRequest, String username) {
        User user = getUserByUsername(username);
        Subject subject = getSubject(id);
        Syllabus syllabus = subject.getSyllabus();

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to update this subject");
        }

        subject.setTitle(subjectRequest.getTitle());
        subject.setDescription(subjectRequest.getDescription());
        
        // Only update order if provided
        if (subjectRequest.getOrder() != null) {
            subject.setOrder(subjectRequest.getOrder());
        }
        
        subject.setTargetCompletionDate(subjectRequest.getTargetCompletionDate());

        Subject updatedSubject = subjectRepository.save(subject);
        return mapToSubjectResponse(updatedSubject);
    }

    @Override
    @Transactional
    public void deleteSubject(Long id, String username) {
        User user = getUserByUsername(username);
        Subject subject = getSubject(id);
        Syllabus syllabus = subject.getSyllabus();

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this subject");
        }

        subjectRepository.delete(subject);
    }

    @Override
    @Transactional
    public void reorderSubjects(Long syllabusId, List<Long> subjectIds, String username) {
        User user = getUserByUsername(username);
        Syllabus syllabus = getSyllabus(syllabusId);

        // Check if user owns this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to reorder subjects in this syllabus");
        }

        // Check if all subject IDs belong to this syllabus
        List<Subject> subjects = subjectRepository.findBySyllabusOrderByOrder(syllabus);
        List<Long> existingIds = subjects.stream().map(Subject::getId).collect(Collectors.toList());

        if (!existingIds.containsAll(subjectIds) || existingIds.size() != subjectIds.size()) {
            throw new BadRequestException("Invalid subject IDs provided for reordering");
        }

        // Update order for each subject
        for (int i = 0; i < subjectIds.size(); i++) {
            Long subjectId = subjectIds.get(i);
            Subject subject = subjects.stream()
                    .filter(s -> s.getId().equals(subjectId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));
            
            subject.setOrder(i + 1);
            subjectRepository.save(subject);
        }
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Syllabus getSyllabus(Long id) {
        return syllabusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
    }

    private Subject getSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
    }

    private SubjectResponse mapToSubjectResponse(Subject subject) {
        Double completionPercentage = progressService.calculateSubjectCompletionPercentage(subject.getId());
        
        return SubjectResponse.builder()
                .id(subject.getId())
                .syllabusId(subject.getSyllabus().getId())
                .title(subject.getTitle())
                .description(subject.getDescription())
                .order(subject.getOrder())
                .targetCompletionDate(subject.getTargetCompletionDate())
                .topicCount(subject.getTopics().size())
                .completionPercentage(completionPercentage)
                .build();
    }
}
