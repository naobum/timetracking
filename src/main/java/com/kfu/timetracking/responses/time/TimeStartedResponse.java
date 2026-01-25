package com.kfu.timetracking.responses.time;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.ResponseBody;

import com.kfu.timetracking.models.TaskType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Ответ при успешном запуске отслеживания времени.
 * Подтверждает начало таймера для студента.
 */
@ResponseBody
@Data
@AllArgsConstructor
@Schema(description = "Ответ при успешном запуске отслеживания времени")
public class TimeStartedResponse {
    
    @Schema(
        description = "ID студента, для которого запущен таймер",
        example = "1",
        required = true
    )
    private Long studentId;
    
    @Schema(
        description = "Дата и время начала отслеживания",
        example = "2026-01-25T10:00:00",
        required = true
    )
    private LocalDateTime start;
    
    @Schema(
        description = "Тип учебной деятельности (LECTURE, PRACTICE, HOMEWORK, PROJECT, EXAM)",
        example = "LECTURE",
        required = true,
        implementation = TaskType.class
    )
    private TaskType taskType;
    
    @Schema(
        description = "Описание выполняемой задачи",
        example = "Лекция по математике",
        required = false
    )
    private String description;
}
