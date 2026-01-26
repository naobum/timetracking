package com.kfu.timetracking.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.kfu.timetracking.exceptions.EntityNotFoundException;
import com.kfu.timetracking.models.SubjectTask;
import com.kfu.timetracking.models.TaskType;
import com.kfu.timetracking.models.TimeEntry;
import com.kfu.timetracking.repositories.SubjectTaskRepository;
import com.kfu.timetracking.repositories.TimeEntryRepository;
import com.kfu.timetracking.responses.predictions.DeadlinePredictionDTO;
import com.kfu.timetracking.responses.predictions.RiskLevel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PredictionService {
    private final TimeEntryRepository timeEntryRepository;
    private final SubjectTaskRepository subjectTaskRepository;

    @Cacheable("time-entries")
    public DeadlinePredictionDTO getPredictionForSubject(String subject, TaskType taskType) {
        
        SubjectTask subjectTask = subjectTaskRepository.findBySubjectAndTaskType(subject, taskType)
            .orElseThrow(() -> new EntityNotFoundException("SubjectTask не найдена для предмета: " + subject + " и типа задачи: " + taskType));
        
        double totalHoursRequired = subjectTask.getExpectedHours();

        List<TimeEntry> entries = timeEntryRepository.findByDescriptionContainingIgnoreCase(subject);
        
        double hoursSpent = entries.stream()
            .filter(entry -> entry.getStart() != null && entry.getEnd() != null)
            .mapToDouble(entry -> 
                Duration.between(entry.getStart(), entry.getEnd()).toMinutes() / 60.0
            )
            .sum();

        double hoursLeft = Math.max(0, totalHoursRequired - hoursSpent);
        long daysLeft = Math.max(0, Duration.between(LocalDateTime.now(), LocalDateTime.now().plusDays(7)).toDays());

        RiskLevel risk = calculateRisk(hoursLeft, daysLeft);

        return new DeadlinePredictionDTO(subject, LocalDateTime.now().plusDays(7), hoursLeft, risk);
    }

    private RiskLevel calculateRisk(double hoursLeft, long daysLeft) {
        if (hoursLeft == 0) {
            return RiskLevel.SAFE;
        }
        if (daysLeft <= 0) {
            return RiskLevel.CRITICAL;
        }

        double hoursPerDayNeeded = hoursLeft / daysLeft;

        if (hoursPerDayNeeded > 6) {
            return RiskLevel.HIGH;
        } else if (hoursPerDayNeeded > 3) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }
}
