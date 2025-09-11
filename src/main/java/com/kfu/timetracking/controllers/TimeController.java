package com.kfu.timetracking.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.kfu.timetracking.responses.time.TimeStartedResponse;
import com.kfu.timetracking.responses.time.TimeStoppedResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/time")
public class TimeController {
    @PostMapping("/start")
    public ResponseEntity<TimeStartedResponse> start(@RequestBody String entity) {
        //TODO: process POST request
        
        return new ResponseEntity<>(HttpStatusCode.valueOf(202));
    }
    
    @PostMapping("/stop")
    public ResponseEntity<TimeStoppedResponse> stop(@RequestBody String entity) {
        //TODO: process POST request
        
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }
    
}
