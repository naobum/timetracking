package com.kfu.timetracking.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.kfu.timetracking.responses.time.TimeStartedResponse;
import com.kfu.timetracking.responses.time.TimeStoppedResponse;
import com.kfu.timetracking.services.TimeService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/time")
public class TimeController {
    private final TimeService timeService;

    public TimeController(TimeService timeService){
        this.timeService = timeService;
    }

    @PostMapping("/start")
    public ResponseEntity<TimeStartedResponse> start() {
        
        return ResponseEntity.status(HttpStatus.CREATED).body(timeService.start());
    }
    
    @PostMapping("/stop")
    public ResponseEntity<TimeStoppedResponse> stop() {
        
        return ResponseEntity.status(HttpStatus.OK).body(timeService.stop());
    }
    
}
