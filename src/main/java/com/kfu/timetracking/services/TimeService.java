package com.kfu.timetracking.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.kfu.timetracking.models.Student;
import com.kfu.timetracking.models.TimeEntry;
import com.kfu.timetracking.repositories.StudentRepository;
import com.kfu.timetracking.repositories.TimeEntryRepository;
import com.kfu.timetracking.requests.timeTrack.StartTimeTrackRequest;
import com.kfu.timetracking.requests.timeTrack.StopTimeTrackRequest;
import com.kfu.timetracking.responses.time.TimeEntryDto;
import com.kfu.timetracking.responses.time.TimeEntryMappings;
import com.kfu.timetracking.responses.time.TimeStartedResponse;
import com.kfu.timetracking.responses.time.TimeStoppedResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeService {
    private final TimeEntryRepository timeEntryRepository;
    private final StudentRepository studentRepo;
    private final TimeEntryMappings timeEntryMapper;

    @Transactional
    @CacheEvict(value = "time-entries", allEntries = true)
    public TimeStartedResponse start(StartTimeTrackRequest request){
        log.info("Попытка начать трекинг времени для студента с ID: {}", request.getStudentId());
        Student student = studentRepo.findById(request.getStudentId())
            .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + request.getStudentId() + " не найден"));

        if (timeEntryRepository.findByStudentAndEndIsNull(student).isPresent()) {
            log.warn("У студента с ID: {} уже есть активный трекинг", request.getStudentId());
            throw new IllegalStateException("У студента уже есть активный трекинг");
        }

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setStudent(student);
        timeEntry.setType(request.getTaskType());
        timeEntry.setStart(LocalDateTime.now());
        timeEntry.setDescription(request.getDescription());
        timeEntry.setBillable(true);

        timeEntryRepository.save(timeEntry);
        log.info("Трекинг времени начат для студента с ID: {} в {}", request.getStudentId(), timeEntry.getStart());

        return new TimeStartedResponse(
            timeEntry.getStudent().getId(),
            timeEntry.getStart(),
            timeEntry.getType(),
            timeEntry.getDescription());
    }

    @Transactional
    @CacheEvict(value = "time-entries", allEntries = true)
    public TimeStoppedResponse stop(StopTimeTrackRequest request){
        log.info("Попытка остановить трекинг времени для студента с ID: {}", request.getStudentId());
        Student student = studentRepo.findById(request.getStudentId())
            .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + request.getStudentId() + " не найден"));

        TimeEntry timeEntry = timeEntryRepository.findByStudentAndEndIsNull(student)
            .orElseThrow(() -> new IllegalStateException("Нет активного трекинга"));
        
        timeEntry.setEnd(LocalDateTime.now());
        timeEntry.setBillable(false);
        log.info("Трекинг времени остановлен для студента с ID: {} в {}", request.getStudentId(), timeEntry.getEnd());

        return new TimeStoppedResponse();
    }

    @Cacheable(value = "time-entries", key = "#studentId")
    public List<TimeEntryDto> getWeeklyStats(Long studentId) {
        log.debug("Получение недельной статистики для студента с ID: {}", studentId);
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime weekStart = today.with(java.time.DayOfWeek.MONDAY);

        Student student = studentRepo.findById(studentId)
            .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + studentId + " не найден"));

        List<TimeEntryDto> result;
        result = timeEntryRepository.findByStudentAndStartBetween(student, weekStart, today)
            .stream().map(timeEntryMapper::ToTimeEntryDto).collect(Collectors.toList());
        
        log.info("Получена недельная статистика для студента с ID: {} - {} записей", studentId, result.size());

        return result;
    }
}
