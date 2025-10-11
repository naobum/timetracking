package com.kfu.timetracking.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.kfu.timetracking.models.Student;
import com.kfu.timetracking.models.TimeEntry;
import com.kfu.timetracking.repositories.StudentRepository;
import com.kfu.timetracking.repositories.TimeTrackingRepository;
import com.kfu.timetracking.requests.timeTrack.StartTimeTrackRequest;
import com.kfu.timetracking.requests.timeTrack.StopTimeTrackRequest;
import com.kfu.timetracking.responses.time.TimeStartedResponse;
import com.kfu.timetracking.responses.time.TimeStoppedResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeService {
    private final TimeTrackingRepository timeEntiesRepo;
    private final StudentRepository studentRepo;

    @Transactional
    public TimeStartedResponse start(StartTimeTrackRequest request){
        Student student = studentRepo.findById(request.getStudentId())
            .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + request.getStudentId() + " не найден"));

        // Проверка: если есть активный трекинг, не создаём новый
        if (timeEntiesRepo.findByStudentAndEndIsNull(student).isPresent()) {
            throw new IllegalStateException("У студента уже есть активный трекинг");
        }

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setStudent(student);
        timeEntry.setType(request.getTaskType());
        timeEntry.setStart(LocalDateTime.now());
        timeEntry.setDescription(request.getDescription());
        timeEntry.setBillable(true);

        timeEntiesRepo.save(timeEntry);

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

        TimeEntry timeEntry = timeEntiesRepo.findByStudentAndEndIsNull(student)
            .orElseThrow(() -> new IllegalStateException("Нет активного трекинга"));
        
        timeEntry.setEnd(LocalDateTime.now());
        timeEntry.setBillable(false);

        return new TimeStoppedResponse();
    }
}
