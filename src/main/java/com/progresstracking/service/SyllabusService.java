package com.progresstracking.service;

import com.progresstracking.dto.syllabus.SyllabusRequest;
import com.progresstracking.dto.syllabus.SyllabusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SyllabusService {

    SyllabusResponse createSyllabus(SyllabusRequest syllabusRequest, String username);
    
    SyllabusResponse getSyllabusById(Long id, String username);
    
    SyllabusResponse updateSyllabus(Long id, SyllabusRequest syllabusRequest, String username);
    
    void deleteSyllabus(Long id, String username);
    
    Page<SyllabusResponse> getUserSyllabi(String username, Pageable pageable);
    
    Page<SyllabusResponse> getPublicSyllabi(Pageable pageable);
    
    SyllabusResponse getSyllabusByShareableLink(String shareableLink);
    
    String generateShareableLink(Long id, String username);
    
    void revokeShareableLink(Long id, String username);
}
