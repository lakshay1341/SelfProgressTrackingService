package com.progresstracking.service.impl;

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
import com.progresstracking.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final ProgressEntryRepository progressEntryRepository;
    private final UserRepository userRepository;
    private final SyllabusRepository syllabusRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final SubTopicRepository subTopicRepository;

    @Override
    @Transactional
    public ProgressEntryResponse createProgressEntry(ProgressEntryRequest progressEntryRequest, String username) {
        User user = getUserByUsername(username);
        
        validateItemAccess(progressEntryRequest.getItemType(), progressEntryRequest.getItemId(), user);
        
        // duplicate entry validation
        Optional<ProgressEntry> existingEntry = progressEntryRepository.findByUserAndItemTypeAndItemIdAndDate(
                user, 
                progressEntryRequest.getItemType(), 
                progressEntryRequest.getItemId(), 
                progressEntryRequest.getDate()
        );
        
        if (existingEntry.isPresent()) {
            throw new BadRequestException("A progress entry already exists for this item on this date");
        }
        
        ProgressEntry progressEntry = ProgressEntry.builder()
                .user(user)
                .itemId(progressEntryRequest.getItemId())
                .itemType(progressEntryRequest.getItemType())
                .date(progressEntryRequest.getDate())
                .status(progressEntryRequest.getStatus())
                .timeSpentMinutes(progressEntryRequest.getTimeSpentMinutes())
                .notes(progressEntryRequest.getNotes())
                .build();
        
        ProgressEntry savedEntry = progressEntryRepository.save(progressEntry);
        return mapToProgressEntryResponse(savedEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgressEntryResponse getProgressEntryById(Long id, String username) {
        User user = getUserByUsername(username);
        ProgressEntry progressEntry = getProgressEntry(id);
        
        //  check  progress entry ownership
        if (!progressEntry.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to access this progress entry");
        }
        
        return mapToProgressEntryResponse(progressEntry);
    }

    @Override
    @Transactional
    public ProgressEntryResponse updateProgressEntry(Long id, ProgressEntryRequest progressEntryRequest, String username) {
        User user = getUserByUsername(username);
        ProgressEntry progressEntry = getProgressEntry(id);

        //  check  progress entry ownership
        if (!progressEntry.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to update this progress entry");
        }
        //validation
        if (!progressEntry.getItemType().equals(progressEntryRequest.getItemType()) ||
            !progressEntry.getItemId().equals(progressEntryRequest.getItemId())) {
            validateItemAccess(progressEntryRequest.getItemType(), progressEntryRequest.getItemId(), user);

        }


        if (!progressEntry.getDate().equals(progressEntryRequest.getDate())) {
            Optional<ProgressEntry> existingEntry = progressEntryRepository.findByUserAndItemTypeAndItemIdAndDate(
                    user, 
                    progressEntryRequest.getItemType(), 
                    progressEntryRequest.getItemId(), 
                    progressEntryRequest.getDate()
            );
            
            if (existingEntry.isPresent() && !existingEntry.get().getId().equals(id)) {
                throw new BadRequestException("A progress entry already exists for this item on this date");
            }
        }
        
        progressEntry.setItemType(progressEntryRequest.getItemType());
        progressEntry.setItemId(progressEntryRequest.getItemId());
        progressEntry.setDate(progressEntryRequest.getDate());
        progressEntry.setStatus(progressEntryRequest.getStatus());
        progressEntry.setTimeSpentMinutes(progressEntryRequest.getTimeSpentMinutes());
        progressEntry.setNotes(progressEntryRequest.getNotes());
        
        ProgressEntry updatedEntry = progressEntryRepository.save(progressEntry);
        return mapToProgressEntryResponse(updatedEntry);
    }

    @Override
    @Transactional
    public void deleteProgressEntry(Long id, String username) {
        User user = getUserByUsername(username);
        ProgressEntry progressEntry = getProgressEntry(id);
        
        // //  check  progress entry ownership
        if (!progressEntry.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this progress entry");
        }
        
        progressEntryRepository.delete(progressEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProgressEntryResponse> getUserProgressEntries(String username, Pageable pageable) {
        User user = getUserByUsername(username);
        Page<ProgressEntry> progressEntries = progressEntryRepository.findByUser(user, pageable);
        return progressEntries.map(this::mapToProgressEntryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgressEntryResponse> getUserProgressEntriesByDateRange(String username, LocalDate startDate, LocalDate endDate) {
        User user = getUserByUsername(username);
        List<ProgressEntry> progressEntries = progressEntryRepository.findByUserAndDateBetween(user, startDate, endDate);
        return progressEntries.stream()
                .map(this::mapToProgressEntryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateSyllabusCompletionPercentage(Long syllabusId) {
        Syllabus syllabus = getSyllabus(syllabusId);
        
        List<Subject> subjects = subjectRepository.findBySyllabusOrderByOrder(syllabus);
        
        if (subjects.isEmpty()) {
            return 0.0;
        }
        
        // calculate average completion % all subjects
        double totalPercentage = 0.0;
        for (Subject subject : subjects) {
            totalPercentage += calculateSubjectCompletionPercentage(subject.getId());
        }
        
        return totalPercentage / subjects.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateSubjectCompletionPercentage(Long subjectId) {
        Subject subject = getSubject(subjectId);
        
        List<Topic> topics = topicRepository.findBySubjectOrderByOrder(subject);
        
        if (topics.isEmpty()) {
            // check for  subject itself
            List<ProgressEntry> subjectEntries = progressEntryRepository.findByUserAndItemTypeAndItemId(
                    subject.getSyllabus().getUser(),
                    ProgressEntry.ItemType.SUBJECT,
                    subjectId
            );
            
            if (subjectEntries.isEmpty()) {
                return 0.0;
            }
            
            // return 100% when  status = completed
            if (subjectEntries.stream().anyMatch(entry -> entry.getStatus() == ProgressEntry.Status.COMPLETED)) {
                return 100.0;
            }
            
            long inProgressCount = subjectEntries.stream()
                    .filter(entry -> entry.getStatus() == ProgressEntry.Status.IN_PROGRESS)
                    .count();
            
            return inProgressCount > 0 ? 50.0 : 0.0;
        }
        
        // Cal. average completion % all topics
        double totalPercentage = 0.0;
        for (Topic topic : topics) {
            totalPercentage += calculateTopicCompletionPercentage(topic.getId());
        }
        
        return totalPercentage / topics.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateTopicCompletionPercentage(Long topicId) {
        Topic topic = getTopic(topicId);
        
        List<SubTopic> subTopics = subTopicRepository.findByTopicOrderByOrder(topic);
        
        if (subTopics.isEmpty()) {
            // checkfor topic
            List<ProgressEntry> topicEntries = progressEntryRepository.findByUserAndItemTypeAndItemId(
                    topic.getSubject().getSyllabus().getUser(),
                    ProgressEntry.ItemType.TOPIC,
                    topicId
            );
            
            if (topicEntries.isEmpty()) {
                return 0.0;
            }
            
            // Return 100% if any entry has COMPLETED status, otherwise calculate based on IN_PROGRESS entries
            if (topicEntries.stream().anyMatch(entry -> entry.getStatus() == ProgressEntry.Status.COMPLETED)) {
                return 100.0;
            }
            
            long inProgressCount = topicEntries.stream()
                    .filter(entry -> entry.getStatus() == ProgressEntry.Status.IN_PROGRESS)
                    .count();
            
            return inProgressCount > 0 ? 50.0 : 0.0;
        }
        
        // Calculate the average completion percentage of all subtopics
        double totalPercentage = 0.0;
        for (SubTopic subTopic : subTopics) {
            totalPercentage += calculateSubTopicCompletionPercentage(subTopic.getId());
        }
        
        return totalPercentage / subTopics.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateSubTopicCompletionPercentage(Long subTopicId) {
        SubTopic subTopic = getSubTopic(subTopicId);
        
        // Check if there are any progress entries for the subtopic
        List<ProgressEntry> subTopicEntries = progressEntryRepository.findByUserAndItemTypeAndItemId(
                subTopic.getTopic().getSubject().getSyllabus().getUser(),
                ProgressEntry.ItemType.SUBTOPIC,
                subTopicId
        );
        
        if (subTopicEntries.isEmpty()) {
            return 0.0;
        }

// return 100% when  status = completed
        if (subTopicEntries.stream().anyMatch(entry -> entry.getStatus() == ProgressEntry.Status.COMPLETED)) {
            return 100.0;
        }
        
        long inProgressCount = subTopicEntries.stream()
                .filter(entry -> entry.getStatus() == ProgressEntry.Status.IN_PROGRESS)
                .count();
        
        return inProgressCount > 0 ? 50.0 : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public CompletionSummaryResponse getSyllabusCompletionSummary(Long syllabusId, String username) {
        User user = getUserByUsername(username);
        Syllabus syllabus = getSyllabus(syllabusId);
        
        if (!syllabus.getUser().getId().equals(user.getId()) && !syllabus.isPublic()) {
            throw new UnauthorizedException("You don't have permission to access this syllabus");
        }
        
        Double overallCompletionPercentage = calculateSyllabusCompletionPercentage(syllabusId);
        
        List<Subject> subjects = subjectRepository.findBySyllabusOrderByOrder(syllabus);
        List<CompletionSummaryResponse.SubjectCompletionDto> subjectCompletions = new ArrayList<>();
        
        for (Subject subject : subjects) {
            Double completionPercentage = calculateSubjectCompletionPercentage(subject.getId());
            
            // cunt completed topics
            List<Topic> topics = topicRepository.findBySubjectOrderByOrder(subject);
            int completedTopics = 0;
            
            for (Topic topic : topics) {
                if (calculateTopicCompletionPercentage(topic.getId()) >= 100.0) {
                    completedTopics++;
                }
            }
            
            subjectCompletions.add(CompletionSummaryResponse.SubjectCompletionDto.builder()
                    .subjectId(subject.getId())
                    .subjectTitle(subject.getTitle())
                    .completionPercentage(completionPercentage)
                    .completedTopics(completedTopics)
                    .totalTopics(topics.size())
                    .build());
        }
        
        return CompletionSummaryResponse.builder()
                .syllabusId(syllabus.getId())
                .syllabusTitle(syllabus.getTitle())
                .overallCompletionPercentage(overallCompletionPercentage)
                .subjectCompletions(subjectCompletions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProgressSummaryResponse getUserProgressSummary(String username, LocalDate startDate, LocalDate endDate) {
        User user = getUserByUsername(username);
        
        List<ProgressEntry> progressEntries = progressEntryRepository.findByUserAndDateBetween(user, startDate, endDate);
        
        Long totalDaysWithProgress = progressEntryRepository.countDistinctDatesByUserAndDateBetween(user, startDate, endDate);
        
        Integer totalTimeSpentMinutes = progressEntryRepository.sumTimeSpentByUserAndDateBetween(user, startDate, endDate);
        if (totalTimeSpentMinutes == null) {
            totalTimeSpentMinutes = 0;
        }
        
        Integer streak = getCurrentStreak(username);
        
        // Group progress entries by date
        Map<LocalDate, List<ProgressEntry>> entriesByDate = progressEntries.stream()
                .collect(Collectors.groupingBy(ProgressEntry::getDate));
        
        // Create daily progress DTOs
        List<ProgressSummaryResponse.DailyProgressDto> dailyProgress = new ArrayList<>();
        
        for (Map.Entry<LocalDate, List<ProgressEntry>> entry : entriesByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<ProgressEntry> entries = entry.getValue();
            
            Integer timeSpentMinutes = entries.stream()
                    .map(ProgressEntry::getTimeSpentMinutes)
                    .filter(Objects::nonNull)
                    .reduce(0, Integer::sum);
            
            dailyProgress.add(ProgressSummaryResponse.DailyProgressDto.builder()
                    .date(date)
                    .timeSpentMinutes(timeSpentMinutes)
                    .itemsProgressed(entries.size())
                    .build());
        }
        
        // Sort daily progress by date
        dailyProgress.sort(Comparator.comparing(ProgressSummaryResponse.DailyProgressDto::getDate));
        
        return ProgressSummaryResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalDaysWithProgress(totalDaysWithProgress.intValue())
                .totalTimeSpentMinutes(totalTimeSpentMinutes)
                .streak(streak)
                .dailyProgress(dailyProgress)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TimeDistributionResponse getUserTimeDistribution(String username) {
        User user = getUserByUsername(username);
        
        // Get all progress entries for the user
        List<ProgressEntry> progressEntries = progressEntryRepository.findByUser(user, Pageable.unpaged()).getContent();
        
        // Calculate total time spent
        Integer totalTimeSpentMinutes = progressEntries.stream()
                .map(ProgressEntry::getTimeSpentMinutes)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
        
        // Group entries by subject
        Map<Long, List<ProgressEntry>> entriesBySubject = new HashMap<>();
        
        for (ProgressEntry entry : progressEntries) {
            if (entry.getTimeSpentMinutes() == null) {
                continue;
            }
            
            Long subjectId = null;
            
            switch (entry.getItemType()) {
                case SUBJECT:
                    subjectId = entry.getItemId();
                    break;
                case TOPIC:
                    Topic topic = getTopic(entry.getItemId());
                    subjectId = topic.getSubject().getId();
                    break;
                case SUBTOPIC:
                    SubTopic subTopic = getSubTopic(entry.getItemId());
                    subjectId = subTopic.getTopic().getSubject().getId();
                    break;
            }
            
            if (subjectId != null) {
                entriesBySubject.computeIfAbsent(subjectId, k -> new ArrayList<>()).add(entry);
            }
        }
        
        // Create subject time distribution DTOs
        List<TimeDistributionResponse.SubjectTimeDto> subjectDistribution = new ArrayList<>();
        
        for (Map.Entry<Long, List<ProgressEntry>> entry : entriesBySubject.entrySet()) {
            Long subjectId = entry.getKey();
            List<ProgressEntry> entries = entry.getValue();
            
            Subject subject = getSubject(subjectId);
            
            Integer timeSpentMinutes = entries.stream()
                    .map(ProgressEntry::getTimeSpentMinutes)
                    .filter(Objects::nonNull)
                    .reduce(0, Integer::sum);
            
            Double percentageOfTotal = totalTimeSpentMinutes > 0 
                    ? (timeSpentMinutes.doubleValue() / totalTimeSpentMinutes) * 100 
                    : 0.0;
            
            subjectDistribution.add(TimeDistributionResponse.SubjectTimeDto.builder()
                    .subjectId(subjectId)
                    .subjectTitle(subject.getTitle())
                    .timeSpentMinutes(timeSpentMinutes)
                    .percentageOfTotal(percentageOfTotal)
                    .build());
        }
        
        // Sort by time spent (descending)
        subjectDistribution.sort(Comparator.comparing(TimeDistributionResponse.SubjectTimeDto::getTimeSpentMinutes).reversed());
        
        return TimeDistributionResponse.builder()
                .totalTimeSpentMinutes(totalTimeSpentMinutes)
                .subjectDistribution(subjectDistribution)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCurrentStreak(String username) {
        User user = getUserByUsername(username);
        
        // Get the most recent dates with progress entries
        List<LocalDate> recentDates = progressEntryRepository.findDistinctDatesByUserOrderByDateDesc(user, Pageable.unpaged());
        
        if (recentDates.isEmpty()) {
            return 0;
        }
        
        // Check if the most recent date is today or yesterday
        LocalDate today = LocalDate.now();
        LocalDate mostRecentDate = recentDates.get(0);
        
        if (!mostRecentDate.equals(today) && !mostRecentDate.equals(today.minusDays(1))) {
            return 0; // Streak broken if no entries today or yesterday
        }
        
        // Count consecutive days
        int streak = 1;
        LocalDate expectedDate = mostRecentDate.minusDays(1);
        
        for (int i = 1; i < recentDates.size(); i++) {
            LocalDate date = recentDates.get(i);
            
            if (date.equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break; // Streak broken
            }
        }
        
        return streak;
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

    private Topic getTopic(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", id));
    }

    private SubTopic getSubTopic(Long id) {
        return subTopicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubTopic", "id", id));
    }

    private ProgressEntry getProgressEntry(Long id) {
        return progressEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProgressEntry", "id", id));
    }

    private void validateItemAccess(ProgressEntry.ItemType itemType, Long itemId, User user) {
        Syllabus syllabus;
        
        switch (itemType) {
            case SUBJECT:
                Subject subject = getSubject(itemId);
                syllabus = subject.getSyllabus();
                break;
            case TOPIC:
                Topic topic = getTopic(itemId);
                syllabus = topic.getSubject().getSyllabus();
                break;
            case SUBTOPIC:
                SubTopic subTopic = getSubTopic(itemId);
                syllabus = subTopic.getTopic().getSubject().getSyllabus();
                break;
            default:
                throw new BadRequestException("Invalid item type");
        }
        
        // Check if user has access to this syllabus
        if (!syllabus.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to track progress for this item");
        }
    }

    private ProgressEntryResponse mapToProgressEntryResponse(ProgressEntry progressEntry) {
        String itemTitle = getItemTitle(progressEntry.getItemType(), progressEntry.getItemId());
        
        return ProgressEntryResponse.builder()
                .id(progressEntry.getId())
                .userId(progressEntry.getUser().getId())
                .itemId(progressEntry.getItemId())
                .itemType(progressEntry.getItemType())
                .itemTitle(itemTitle)
                .date(progressEntry.getDate())
                .status(progressEntry.getStatus())
                .timeSpentMinutes(progressEntry.getTimeSpentMinutes())
                .notes(progressEntry.getNotes())
                .build();
    }

    private String getItemTitle(ProgressEntry.ItemType itemType, Long itemId) {
        switch (itemType) {
            case SUBJECT:
                return getSubject(itemId).getTitle();
            case TOPIC:
                return getTopic(itemId).getTitle();
            case SUBTOPIC:
                return getSubTopic(itemId).getTitle();
            default:
                return "Unknown";
        }
    }
}
