package com.kfu.timetracking.responses.time;

import org.springframework.stereotype.Component;

import com.kfu.timetracking.models.TimeEntry;

@Component
public class TimeEntryMappings {
    public TimeEntryDto ToTimeEntryDto(TimeEntry timeEntry){
        return new TimeEntryDto(
            timeEntry.getId(),
            timeEntry.getType(),
            timeEntry.getDescription(),
            timeEntry.getStart(),
            timeEntry.getEnd()
        );
    }
}
