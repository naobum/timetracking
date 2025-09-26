package com.kfu.timetracking.responses.time;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.ResponseBody;

import com.kfu.timetracking.models.TaskType;

import lombok.AllArgsConstructor;
import lombok.Data;

@ResponseBody
@Data
@AllArgsConstructor
public class TimeStartedResponse {
    private Long studentId;
    private LocalDateTime start;
    private TaskType taskType;
    private String description;
}
