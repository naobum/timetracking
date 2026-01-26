package com.kfu.timetracking.services;

import com.kfu.timetracking.models.Student;
import com.kfu.timetracking.models.TaskType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TimeServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TimeEntryMappings timeEntryMapper;

    @InjectMocks
    private TimeService timeService;

    private Student testStudent;
    private TimeEntry testTimeEntry;
    private StartTimeTrackRequest startRequest;
    private StopTimeTrackRequest stopRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setName("John Doe");

        testTimeEntry = new TimeEntry();
        testTimeEntry.setId(1L);
        testTimeEntry.setStudent(testStudent);
        testTimeEntry.setType(TaskType.PROGRAMMING);
        testTimeEntry.setStart(LocalDateTime.now());
        testTimeEntry.setDescription("Test task");
        testTimeEntry.setBillable(true);

        startRequest = new StartTimeTrackRequest();
        startRequest.setStudentId(1L);
        startRequest.setTaskType(TaskType.PROGRAMMING);
        startRequest.setDescription("Test task");

        stopRequest = new StopTimeTrackRequest();
        stopRequest.setStudentId(1L);
    }

    @Test
    void testStartTimeTrackingSuccess() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(timeEntryRepository.findByStudentAndEndIsNull(testStudent)).thenReturn(Optional.empty());
        when(timeEntryRepository.save(any(TimeEntry.class))).thenReturn(testTimeEntry);

        TimeStartedResponse response = timeService.start(startRequest);

        assertNotNull(response);
        assertEquals(1L, response.getStudentId());
        assertEquals(TaskType.PROGRAMMING, response.getTaskType());
        assertEquals("Test task", response.getDescription());
        assertNotNull(response.getStart());
        verify(timeEntryRepository, times(1)).save(any(TimeEntry.class));
    }

    @Test
    void testStartTimeTrackingStudentNotFound() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        startRequest.setStudentId(999L);
        assertThrows(EntityNotFoundException.class, () -> timeService.start(startRequest));
        verify(timeEntryRepository, never()).save(any());
    }

    @Test
    void testStartTimeTrackingActiveTracking() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(timeEntryRepository.findByStudentAndEndIsNull(testStudent))
                .thenReturn(Optional.of(testTimeEntry));

        assertThrows(IllegalStateException.class, () -> timeService.start(startRequest));
        verify(timeEntryRepository, never()).save(any());
    }

    @Test
    void testStopTimeTrackingSuccess() {
        TimeEntry activeEntry = new TimeEntry();
        activeEntry.setId(1L);
        activeEntry.setStudent(testStudent);
        activeEntry.setStart(LocalDateTime.now().minusHours(1));
        activeEntry.setEnd(null);
        activeEntry.setBillable(true);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(timeEntryRepository.findByStudentAndEndIsNull(testStudent))
                .thenReturn(Optional.of(activeEntry));

        TimeStoppedResponse response = timeService.stop(stopRequest);

        assertNotNull(response);
        assertNotNull(activeEntry.getEnd());
        assertFalse(activeEntry.isBillable());
    }

    @Test
    void testStopTimeTrackingStudentNotFound() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        stopRequest.setStudentId(999L);
        assertThrows(EntityNotFoundException.class, () -> timeService.stop(stopRequest));
    }

    @Test
    void testStopTimeTrackingNoActiveTracking() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(timeEntryRepository.findByStudentAndEndIsNull(testStudent))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> timeService.stop(stopRequest));
    }

    @Test
    void testGetWeeklyStatsSuccess() {
        List<TimeEntry> entries = List.of(testTimeEntry);
        TimeEntryDto dto = new TimeEntryDto();
        dto.setId(1L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(timeEntryRepository.findByStudentAndStartBetween(eq(testStudent), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(entries);
        when(timeEntryMapper.ToTimeEntryDto(testTimeEntry)).thenReturn(dto);

        List<TimeEntryDto> result = timeService.getWeeklyStats(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testGetWeeklyStatsStudentNotFound() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> timeService.getWeeklyStats(999L));
    }

    @Test
    void testGetWeeklyStatsEmpty() {
        LocalDateTime now = LocalDateTime.now();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(timeEntryRepository.findByStudentAndStartBetween(eq(testStudent), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<TimeEntryDto> result = timeService.getWeeklyStats(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStartTimeTrackingWithDifferentTaskTypes() {
        for (TaskType taskType : TaskType.values()) {
            startRequest.setTaskType(taskType);

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(timeEntryRepository.findByStudentAndEndIsNull(testStudent)).thenReturn(Optional.empty());
            when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> {
                TimeEntry entry = invocation.getArgument(0);
                entry.setId(1L);
                return entry;
            });

            TimeStartedResponse response = timeService.start(startRequest);

            assertNotNull(response);
            assertEquals(taskType, response.getTaskType());
        }
    }

    @Test
    void testTimeEntryDurationCalculation() {
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 25, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 25, 11, 30);

        TimeEntry entry = new TimeEntry();
        entry.setStart(startTime);
        entry.setEnd(endTime);

        long durationMinutes = java.time.Duration.between(entry.getStart(), entry.getEnd()).toMinutes();

        assertEquals(150, durationMinutes);
        assertEquals(2.5, durationMinutes / 60.0);
    }
}
