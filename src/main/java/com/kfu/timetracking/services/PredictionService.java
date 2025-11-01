package com.kfu.timetracking.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.kfu.timetracking.models.TimeEntry;
import com.kfu.timetracking.repositories.TimeEntryRepository;
import com.kfu.timetracking.responses.predictions.DeadlinePredictionDTO;
import com.kfu.timetracking.responses.predictions.RiskLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PredictionService {
    private final TimeEntryRepository timeEntryRepository;

    // TODO: придумать как сделать рассчёт времени дедлайна через репозиторий. пока используются моки.
    private static final Map<String, MockSubjectInfo> subjectMetadata = Map.of(
            "ивт", new MockSubjectInfo(40.0, LocalDateTime.now().plusDays(10)),
            "философия", new MockSubjectInfo(20.0, LocalDateTime.now().plusDays(5)),
            "программирование", new MockSubjectInfo(80.0, LocalDateTime.now().plusDays(3))
    );

    @Cacheable("time-entries")
    public DeadlinePredictionDTO getPredictionForSubject(String subject) {
        
        MockSubjectInfo metadata = subjectMetadata.getOrDefault(
                subject.toLowerCase(), 
                new MockSubjectInfo(30.0, LocalDateTime.now().plusDays(7))
        );
        
        LocalDateTime deadline = metadata.getDeadline();
        double totalHoursRequired = metadata.getTotalHours();

        List<TimeEntry> entries = timeEntryRepository.findByDescriptionContainingIgnoreCase(subject);
        
        double hoursSpent = entries.stream()
            .filter(entry -> entry.getStart() != null && entry.getEnd() != null)
            .mapToDouble(entry -> 
                Duration.between(entry.getStart(), entry.getEnd()).toMinutes() / 60.0
            )
            .sum();

        double hoursLeft = Math.max(0, totalHoursRequired - hoursSpent);
        long daysLeft = Math.max(0, Duration.between(LocalDateTime.now(), deadline).toDays());

        RiskLevel risk = calculateRisk(hoursLeft, daysLeft);

        return new DeadlinePredictionDTO(subject, deadline, hoursLeft, risk);
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
    
    // временный мок, пока не сделан рассчёт дедлайнов.
    @Getter
    @AllArgsConstructor
    private static class MockSubjectInfo {
        private final double totalHours;
        private final LocalDateTime deadline;
    }
}
