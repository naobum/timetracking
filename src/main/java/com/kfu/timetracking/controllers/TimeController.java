package com.kfu.timetracking.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.kfu.timetracking.requests.timeTrack.StartTimeTrackRequest;
import com.kfu.timetracking.requests.timeTrack.StopTimeTrackRequest;
import com.kfu.timetracking.responses.time.TimeEntryDto;
import com.kfu.timetracking.responses.time.TimeStartedResponse;
import com.kfu.timetracking.responses.time.TimeStoppedResponse;
import com.kfu.timetracking.services.TimeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/time")
@Tag(name = "TimeEntries", description = "Трекинг времени")
public class TimeController {
    private final TimeService timeService;

    public TimeController(TimeService timeService){
        this.timeService = timeService;
    }

    @PostMapping("/start")
    @PreAuthorize("hasAuthority('TIME:WRITE')")
    @Operation(summary = "Старт трекинга", description = "Возвращает информацию о трекинге")
    public ResponseEntity<TimeStartedResponse> start(@RequestBody StartTimeTrackRequest request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(timeService.start(request));
    }
    
    @PostMapping("/stop")
     @PreAuthorize("hasAuthority('TIME:WRITE')")
    @Operation(summary = "Конец трекинга", description = "Возвращает информацию о трекинге")
    public ResponseEntity<TimeStoppedResponse> stop(@RequestBody StopTimeTrackRequest request) {
        
        return ResponseEntity.status(HttpStatus.OK)
            .body(timeService.stop(request));
    }

    @GetMapping("/weekly")
     @PreAuthorize("hasAuthority('TIME:READ')")
    @Operation(summary = "Получение недельной статистики", 
            description = "Возвращает информацию о всех трекингах за последнюю неделю")
    public ResponseEntity<List<TimeEntryDto>> getWeeklyReport(@RequestParam Long studentId) {
        List<TimeEntryDto> entries = timeService.getWeeklyStats(studentId);
        return ResponseEntity.ok(entries);
    }
}
