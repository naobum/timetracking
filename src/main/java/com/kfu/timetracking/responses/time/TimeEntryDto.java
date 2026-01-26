package com.kfu.timetracking.responses.time;

import java.time.LocalDateTime;

import com.kfu.timetracking.models.TaskType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для передачи информации о записи трекинга времени.
 * Содержит информацию о времени начала и окончания учебной деятельности.
 */
@Data
@AllArgsConstructor
@Schema(description = "Запись о затраченном времени на учебную деятельность")
public class TimeEntryDto {
    public TimeEntryDto() {
        //Default constructor
    }

    @Schema(
        description = "Уникальный идентификатор записи о времени",
        example = "1",
        required = true
    )
    private Long id;

    @Schema(
        description = "Тип учебной деятельности (LECTURE, PRACTICE, HOMEWORK, PROJECT, EXAM)",
        example = "LECTURE",
        required = true,
        implementation = TaskType.class
    )
    private TaskType type;

    @Schema(
        description = "Описание выполняемой задачи",
        example = "Лекция по математике",
        required = false
    )
    private String description;

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
}
