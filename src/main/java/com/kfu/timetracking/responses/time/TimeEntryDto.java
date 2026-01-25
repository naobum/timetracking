package com.kfu.timetracking.responses.time;

import java.time.LocalDateTime;

import com.kfu.timetracking.models.TaskType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeEntryDto {
    public TimeEntryDto() {
        //TODO Auto-generated constructor stub
    }

    private Long id;

    private TaskType type;

    private String description;

    private LocalDateTime start;

    private LocalDateTime end;
}
