package com.kfu.timetracking.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.kfu.timetracking.models.TimeEntry;
import com.kfu.timetracking.repositories.TimeTrackingRepository;
import com.kfu.timetracking.requests.StartTimeTrackRequest;
import com.kfu.timetracking.requests.StopTimeTrackRequest;
import com.kfu.timetracking.responses.time.TimeStartedResponse;
import com.kfu.timetracking.responses.time.TimeStoppedResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeService {
    private final TimeTrackingRepository repo;

    public TimeStartedResponse start(StartTimeTrackRequest request){
        // Проверка: если есть активный трекинг, не создаём новый
        if (repo.findByStudentIdAndEndIsNull(request.getStudentId()).isPresent()) {
            throw new IllegalStateException("У студента уже есть активный трекинг");
        }

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setStudentId(request.getStudentId());
        timeEntry.setType(request.getTaskType());
        timeEntry.setStart(LocalDateTime.now());
        timeEntry.setDescription(request.getDescription());
        timeEntry.setBillable(true);

        repo.save(timeEntry);

        return new TimeStartedResponse(
            timeEntry.getStudentId(),
            timeEntry.getStart(),
            timeEntry.getType(),
            timeEntry.getDescription());
    }

    @Transactional
    public TimeStoppedResponse stop(StopTimeTrackRequest request){
        TimeEntry timeEntry = repo.findByStudentIdAndEndIsNull(request.getStudentId())
            .orElseThrow(() -> new IllegalStateException("Нет активного трекинга"));
        
        timeEntry.setEnd(LocalDateTime.now());
        timeEntry.setBillable(false);

        return new TimeStoppedResponse();
    }
}
