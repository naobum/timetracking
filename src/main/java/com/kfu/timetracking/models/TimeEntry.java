package com.kfu.timetracking.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TimeEntry {
    private Long id;
    private Student student;
    private TaskType type;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean isBillable; // учётное время
}
