package com.kfu.timetracking.services;

import org.springframework.stereotype.Service;

import com.kfu.timetracking.responses.time.TimeStartedResponse;
import com.kfu.timetracking.responses.time.TimeStoppedResponse;

@Service
public class TimeService {
    public TimeStartedResponse start(){
        return new TimeStartedResponse();
    }

    public TimeStoppedResponse stop(){
        return new TimeStoppedResponse();
    }
}
