package com.progresstracking.service;

import com.progresstracking.dto.subject.SubjectRequest;
import com.progresstracking.dto.subject.SubjectResponse;

import java.util.List;

public interface SubjectService {

    SubjectResponse createSubject(Long syllabusId, SubjectRequest subjectRequest, String username);
    
    SubjectResponse getSubjectById(Long id, String username);
    
    List<SubjectResponse> getSubjectsBySyllabus(Long syllabusId, String username);
    
    SubjectResponse updateSubject(Long id, SubjectRequest subjectRequest, String username);
    
    void deleteSubject(Long id, String username);
    
    void reorderSubjects(Long syllabusId, List<Long> subjectIds, String username);
}
