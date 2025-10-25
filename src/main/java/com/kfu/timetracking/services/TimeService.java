package com.kfu.timetracking.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

@Service
@RequiredArgsConstructor
public class TimeService {
    private final TimeEntryRepository timeEntryRepository;
    private final StudentRepository studentRepo;
    private final TimeEntryMappings timeEntryMapper;

    @Transactional
    public TimeStartedResponse start(StartTimeTrackRequest request){
        Student student = studentRepo.findById(request.getStudentId())
            .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + request.getStudentId() + " не найден"));

        if (timeEntryRepository.findByStudentAndEndIsNull(student).isPresent()) {
            throw new IllegalStateException("У студента уже есть активный трекинг");
        }

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setStudent(student);
        timeEntry.setType(request.getTaskType());
        timeEntry.setStart(LocalDateTime.now());
        timeEntry.setDescription(request.getDescription());
        timeEntry.setBillable(true);

        timeEntryRepository.save(timeEntry);

        return new TimeStartedResponse(
            timeEntry.getStudent().getId(),
            timeEntry.getStart(),
            timeEntry.getType(),
            timeEntry.getDescription());
    }

    @Transactional
    public TimeStoppedResponse stop(StopTimeTrackRequest request){
        Student student = studentRepo.findById(request.getStudentId())
            .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + request.getStudentId() + " не найден"));

        TimeEntry timeEntry = timeEntryRepository.findByStudentAndEndIsNull(student)
            .orElseThrow(() -> new IllegalStateException("Нет активного трекинга"));
        
        timeEntry.setEnd(LocalDateTime.now());
        timeEntry.setBillable(false);

        return new TimeStoppedResponse();
    }

    public List<TimeEntryDto> getWeeklyStats(Long studentId) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime weekStart = today.with(java.time.DayOfWeek.MONDAY);

        Student student = studentRepo.findById(studentId)
            .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + studentId + " не найден"));

        List<TimeEntryDto> result;
        result = timeEntryRepository.findByStudentAndStartBetween(student, weekStart, today)
            .stream().map(timeEntryMapper::ToTimeEntryDto).collect(Collectors.toList());

        return result;
    }
}
