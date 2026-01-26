package com.kfu.timetracking.responses.time;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.ResponseBody;

import com.kfu.timetracking.models.TaskType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ при успешном завершении отслеживания времени.
 * Подтверждает окончание таймера и сохранение записи.
 */
@ResponseBody
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ при успешном завершении отслеживания времени")
public class TimeStoppedResponse {
    
    public TimeStoppedResponse(Long id, LocalDateTime start, LocalDateTime end, TaskType type, String description) {
        this.studentId = id;
        this.start = start;
        this.end = end;
        this.taskType = type;
        this.description = description;
    }

    @Schema(
        description = "ID студента, для которого остановлен таймер",
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
        description = "Дата и время окончания отслеживания",
        example = "2026-01-25T11:30:00",
        required = true
    )
    private LocalDateTime end;
    
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
    
    @Schema(
        description = "Общее затраченное время в минутах",
        example = "90",
        required = true
    )
    private Long totalMinutes;
}
