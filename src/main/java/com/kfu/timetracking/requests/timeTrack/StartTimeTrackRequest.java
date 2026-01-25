package com.kfu.timetracking.requests.timeTrack;

import com.kfu.timetracking.models.TaskType;

import lombok.Data;

@Data
public class StartTimeTrackRequest {
    private Long studentId;
    private TaskType taskType;
    private String description;
}
