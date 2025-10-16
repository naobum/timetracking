package com.kfu.timetracking.responses.time;

import java.time.LocalDateTime;

import com.kfu.timetracking.models.TaskType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeEntryDto {
    private Long id;

    private TaskType type;

    private String description;

    private LocalDateTime start;

    private LocalDateTime end;
}
