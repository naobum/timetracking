package com.kfu.timetracking.responses.predictions;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи прогноза успеваемости по предмету.
 * Содержит информацию о дедлайне, оставшемся времени и уровне риска.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Прогноз успеваемости студента по предмету")
public class DeadlinePredictionDTO {
    
    @Schema(
        description = "Название предмета",
        example = "ИВТ",
        required = true
    )
    private String subject;
    
    @Schema(
        description = "Предполагаемая дата дедлайна для выполнения задания",
        example = "2026-02-15T23:59:59",
        required = true
    )
    private LocalDateTime deadline;
    
    @Schema(
        description = "Оставшееся время до дедлайна в часах",
        example = "45.5",
        required = true
    )
    private Double hoursLeft;
    
    @Schema(
        description = "Уровень риска невыполнения задания вовремя " +
                      "(SAFE - безопасно, LOW - низкий, MEDIUM - средний, HIGH - высокий, CRITICAL - критический)",
        example = "MEDIUM",
        required = true,
        implementation = RiskLevel.class
    )
    private RiskLevel riskLevel;
}
