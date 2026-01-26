package com.kfu.timetracking.requests.timeTrack;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Запрос для завершения отслеживания времени.
 * Содержит ID студента для остановки его активного таймера.
 */
@Data
@Schema(
    description = "Запрос для завершения отслеживания времени",
    example = "{\"studentId\": 1}"
)
public class StopTimeTrackRequest {
    
    @NotNull(message = "ID студента не может быть пустым")
    @Schema(
        description = "Уникальный идентификатор студента, у которого нужно остановить таймер",
        example = "1",
        required = true
    )
    private Long studentId;
}
