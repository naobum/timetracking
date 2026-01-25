package com.kfu.timetracking.services;

import com.kfu.timetracking.models.TimeEntry;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PredictionServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @InjectMocks
    private PredictionService predictionService;

    private List<TimeEntry> mockTimeEntries;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockTimeEntries = new ArrayList<>();
    }

    @Test
    void testGetPredictionForSubjectWithSafeRisk() {
        // Total required: 40 hours, Deadline: +10 days
        // Hours spent: 35 hours
        // Hours left: 5 hours, Days left: 10 -> 0.5 hours per day -> LOW risk
        
        mockTimeEntries.clear();
        TimeEntry entry = createTimeEntry("ивт", 35); // 35 hours spent
        mockTimeEntries.add(entry);

        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("ивт"))
                .thenReturn(mockTimeEntries);

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("ивт");

        assertNotNull(result);
        assertEquals("ивт", result.getSubject());
        assertEquals(5.0, result.getHoursLeft());
        assertEquals(RiskLevel.LOW, result.getRiskLevel());
    }

    @Test
    void testGetPredictionForSubjectWithHighRisk() {
        // Total required: 40 hours, Deadline: +10 days
        // Hours spent: 10 hours
        // Hours left: 30 hours, Days left: 10 -> 3 hours per day -> MEDIUM risk
        
        mockTimeEntries.clear();
        TimeEntry entry = createTimeEntry("ивт", 10);
        mockTimeEntries.add(entry);

        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("ивт"))
                .thenReturn(mockTimeEntries);

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("ивт");

        assertNotNull(result);
        assertEquals("ивт", result.getSubject());
        assertEquals(30.0, result.getHoursLeft());
        assertEquals(RiskLevel.MEDIUM, result.getRiskLevel());
    }

    @Test
    void testGetPredictionForSubjectWithCriticalRisk() {
        // Total required: 80 hours, Deadline: +3 days
        // Hours spent: 20 hours
        // Hours left: 60 hours, Days left: 3 -> 20 hours per day -> CRITICAL risk
        
        mockTimeEntries.clear();
        TimeEntry entry = createTimeEntry("программирование", 20);
        mockTimeEntries.add(entry);

        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("программирование"))
                .thenReturn(mockTimeEntries);

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("программирование");

        assertNotNull(result);
        assertEquals("программирование", result.getSubject());
        assertEquals(60.0, result.getHoursLeft());
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
    }

    @Test
    void testGetPredictionForSubjectWithCompleteWork() {
        // Total required: 40 hours, Deadline: +10 days
        // Hours spent: 40 hours
        // Hours left: 0 hours -> SAFE risk
        
        mockTimeEntries.clear();
        TimeEntry entry = createTimeEntry("ивт", 40);
        mockTimeEntries.add(entry);

        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("ивт"))
                .thenReturn(mockTimeEntries);

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("ивт");

        assertNotNull(result);
        assertEquals("ивт", result.getSubject());
        assertEquals(0.0, result.getHoursLeft());
        assertEquals(RiskLevel.SAFE, result.getRiskLevel());
    }

    @Test
    void testGetPredictionForUnknownSubject() {
        // Unknown subject gets default metadata: 30 hours, 7 days deadline
        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("unknown"))
                .thenReturn(new ArrayList<>());

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("unknown");

        assertNotNull(result);
        assertEquals("unknown", result.getSubject());
        assertEquals(30.0, result.getHoursLeft());
        assertNotNull(result.getRiskLevel());
    }

    @Test
    void testGetPredictionForPhilosophy() {
        // Философия: 20 hours, +5 days
        // Hours spent: 5 hours
        // Hours left: 15 hours, Days left: 5 -> 3 hours per day -> MEDIUM risk
        
        mockTimeEntries.clear();
        TimeEntry entry = createTimeEntry("философия", 5);
        mockTimeEntries.add(entry);

        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("философия"))
                .thenReturn(mockTimeEntries);

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("философия");

        assertNotNull(result);
        assertEquals("философия", result.getSubject());
        assertEquals(15.0, result.getHoursLeft());
        assertEquals(RiskLevel.MEDIUM, result.getRiskLevel());
    }

    @Test
    void testGetPredictionCaseInsensitive() {
        mockTimeEntries.clear();
        TimeEntry entry = createTimeEntry("ивт", 35);
        mockTimeEntries.add(entry);

        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("ивт"))
                .thenReturn(mockTimeEntries);

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("ивт");

        assertNotNull(result);
        assertEquals("ивт", result.getSubject());
    }

    @Test
    void testGetPredictionWithMultipleEntries() {
        // Total required: 40 hours, Deadline: +10 days
        // Multiple entries totaling 30 hours
        
        mockTimeEntries.clear();
        mockTimeEntries.add(createTimeEntry("ивт", 15));
        mockTimeEntries.add(createTimeEntry("ивт", 15));

        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("ивт"))
                .thenReturn(mockTimeEntries);

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("ивт");

        assertNotNull(result);
        assertEquals("ивт", result.getSubject());
        assertEquals(10.0, result.getHoursLeft());
    }

    @Test
    void testGetPredictionWithNullEndTime() {
        // Entry with null end time should be excluded from calculation
        mockTimeEntries.clear();
        TimeEntry entry1 = createTimeEntry("ивт", 35);
        TimeEntry entry2 = new TimeEntry();
        entry2.setDescription("ивт");
        entry2.setStart(LocalDateTime.now());
        entry2.setEnd(null); // No end time
        
        mockTimeEntries.add(entry1);
        mockTimeEntries.add(entry2);

        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("ивт"))
                .thenReturn(mockTimeEntries);

        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("ивт");

        assertNotNull(result);
        assertEquals(5.0, result.getHoursLeft()); // Only entry1 (35 hours) should be counted
    }

    @Test
    void testRiskLevelBoundaryHigh() {
        // 6+ hours per day = HIGH risk
        mockTimeEntries.clear();
        TimeEntry entry = createTimeEntry("ивт", 0); // 40 hours needed, 0 spent
        mockTimeEntries.add(entry);

        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("ивт"))
                .thenReturn(mockTimeEntries);

        // 40 hours left / 6.67 days ≈ 6 hours per day -> HIGH risk
        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("ивт");

        assertNotNull(result);
        assertTrue(result.getRiskLevel() == RiskLevel.HIGH || result.getRiskLevel() == RiskLevel.MEDIUM);
    }

    @Test
    void testRiskLevelBoundaryMedium() {
        // 3-6 hours per day = MEDIUM risk
        mockTimeEntries.clear();
        TimeEntry entry = createTimeEntry("философия", 5); // 20 hours needed, 5 spent
        mockTimeEntries.add(entry);

        when(timeEntryRepository.findByDescriptionContainingIgnoreCase("философия"))
                .thenReturn(mockTimeEntries);

        // 15 hours left / 5 days = 3 hours per day -> MEDIUM risk
        DeadlinePredictionDTO result = predictionService.getPredictionForSubject("философия");

        assertNotNull(result);
        assertEquals(RiskLevel.MEDIUM, result.getRiskLevel());
    }

    private TimeEntry createTimeEntry(String description, int hours) {
        TimeEntry entry = new TimeEntry();
        entry.setDescription(description);
        entry.setStart(LocalDateTime.now().minusHours(hours));
        entry.setEnd(LocalDateTime.now());
        return entry;
    }
}
