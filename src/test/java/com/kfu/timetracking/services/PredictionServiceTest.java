package com.kfu.timetracking.services;

import com.kfu.timetracking.exceptions.EntityNotFoundException;
import com.kfu.timetracking.models.SubjectTask;
import com.kfu.timetracking.models.TaskType;
import com.kfu.timetracking.models.TimeEntry;
import com.kfu.timetracking.repositories.SubjectTaskRepository;
import com.kfu.timetracking.repositories.TimeEntryRepository;
import com.kfu.timetracking.responses.predictions.DeadlinePredictionDTO;
import com.kfu.timetracking.responses.predictions.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PredictionServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private SubjectTaskRepository subjectTaskRepository;

    @InjectMocks
    private PredictionService predictionService;

    private List<TimeEntry> mockTimeEntries;
    private SubjectTask mockSubjectTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockTimeEntries = new ArrayList<>();
        mockSubjectTask = new SubjectTask();
    }

    @Test
    void testGetPredictionForSubjectWithLowRisk() {
        // Total required: 40 hours
        // Hours spent: 35 hours
        // Hours left: 5 hours, Days left: 7 -> 0.7 hours per day -> LOW risk
        
        mockSubjectTask.setId(1L);
        mockSubjectTask.setTaskType(TaskType.LAB);
        mockSubjectTask.setSubject("ивт");
        mockSubjectTask.setExpectedHours(40.0);

        mockTimeEntries.clear();
        TimeEntry entry = createTimeEntry("ивт", 35); // 35 hours spent
        mockTimeEntries.add(entry);

        when(subjectTaskRepository.findBySubjectAndTaskType("ивт", TaskType.LAB))
                .thenReturn(Optional.of(mockSubjectTask));
        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("ивт"))
                .thenReturn(mockTimeEntries);

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("ивт", TaskType.LAB);

        assertNotNull(result);
        assertEquals("ивт", result.getSubject());
        assertEquals(5.0, result.getHoursLeft());
        assertEquals(RiskLevel.LOW, result.getRiskLevel());
    }

    @Test
    void testGetPredictionForSubjectWithMediumRisk() {
        // Total required: 40 hours
        // Hours spent: 10 hours
        // Hours left: 30 hours, Days left: 7 -> 4.3 hours per day -> MEDIUM risk
        
        mockSubjectTask.setId(1L);
        mockSubjectTask.setTaskType(TaskType.LAB);
        mockSubjectTask.setSubject("ивт");
        mockSubjectTask.setExpectedHours(40.0);

        mockTimeEntries.clear();
        TimeEntry entry = createTimeEntry("ивт", 10);
        mockTimeEntries.add(entry);

        when(subjectTaskRepository.findBySubjectAndTaskType("ивт", TaskType.LAB))
                .thenReturn(Optional.of(mockSubjectTask));
        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("ивт"))
                .thenReturn(mockTimeEntries);

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("ивт", TaskType.LAB);

        assertNotNull(result);
        assertEquals("ивт", result.getSubject());
        assertEquals(30.0, result.getHoursLeft());
        assertEquals(RiskLevel.MEDIUM, result.getRiskLevel());
    }

    @Test
    void testGetPredictionForSubjectWithHighRisk() {
        // Total required: 80 hours
        // Hours spent: 20 hours
        // Hours left: 60 hours, Days left: 7 -> 8.6 hours per day -> HIGH risk
        
        mockSubjectTask.setId(2L);
        mockSubjectTask.setTaskType(TaskType.PROJECT);
        mockSubjectTask.setSubject("программирование");
        mockSubjectTask.setExpectedHours(80.0);

        mockTimeEntries.clear();
        TimeEntry entry = createTimeEntry("программирование", 20);
        mockTimeEntries.add(entry);

        when(subjectTaskRepository.findBySubjectAndTaskType("программирование", TaskType.PROJECT))
                .thenReturn(Optional.of(mockSubjectTask));
        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("программирование"))
                .thenReturn(mockTimeEntries);

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("программирование", TaskType.PROJECT);

        assertNotNull(result);
        assertEquals("программирование", result.getSubject());
        assertEquals(60.0, result.getHoursLeft());
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
    }

    @Test
    void testGetPredictionForSubjectWithCompleteWork() {
        // Total required: 40 hours
        // Hours spent: 40 hours
        // Hours left: 0 hours -> SAFE risk
        
        mockSubjectTask.setId(1L);
        mockSubjectTask.setTaskType(TaskType.LAB);
        mockSubjectTask.setSubject("ивт");
        mockSubjectTask.setExpectedHours(40.0);

        mockTimeEntries.clear();
        TimeEntry entry = createTimeEntry("ивт", 40);
        mockTimeEntries.add(entry);

        when(subjectTaskRepository.findBySubjectAndTaskType("ивт", TaskType.LAB))
                .thenReturn(Optional.of(mockSubjectTask));
        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("ивт"))
                .thenReturn(mockTimeEntries);

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("ивт", TaskType.LAB);

        assertNotNull(result);
        assertEquals("ивт", result.getSubject());
        assertEquals(0.0, result.getHoursLeft());
        assertEquals(RiskLevel.SAFE, result.getRiskLevel());
    }

    @Test
    void testGetPredictionForNotFoundSubjectTask() {
        // SubjectTask не найдена в БД -> выброс исключения
        when(subjectTaskRepository.findBySubjectAndTaskType("unknown", TaskType.LAB))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            predictionService.getPredictionForSubject("unknown", TaskType.LAB);
        });
    }

    private TimeEntry createTimeEntry(String description, int hours) {
        TimeEntry entry = new TimeEntry();
        entry.setDescription(description);
        entry.setStart(LocalDateTime.now().minusHours(hours));
        entry.setEnd(LocalDateTime.now());
        return entry;
    }
}
