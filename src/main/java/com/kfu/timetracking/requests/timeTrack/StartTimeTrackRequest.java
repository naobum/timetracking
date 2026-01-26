package com.kfu.timetracking.requests.timeTrack;

import com.kfu.timetracking.models.TaskType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Запрос для начала отслеживания времени.
 * Содержит информацию о студенте и типе задачи.
 */
@Data
@Schema(
    description = "Запрос для начала отслеживания времени учебной деятельности",
    example = "{\"studentId\": 1, \"taskType\": \"LECTURE\", \"description\": \"Лекция по математике\"}"
)
public class StartTimeTrackRequest {
    
    @NotNull(message = "ID студента не может быть пустым")
    @Schema(
        description = "Уникальный идентификатор студента",
        example = "1",
        required = true
    )
    private Long studentId;
    
    @NotNull(message = "Тип задачи не может быть пустым")
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
