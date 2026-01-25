package com.kfu.timetracking.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kfu.timetracking.responses.predictions.DeadlinePredictionDTO;
import com.kfu.timetracking.services.PredictionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

/**
 * REST контроллер для управления прогнозами успеваемости студентов.
 * Предоставляет прогнозы по приблизительному времени выполнения заданий
 * и уровню риска на основе истории трекинга времени.
 */
@RestController
@RequestMapping("/api/predictions")
@Tag(
    name = "Predictions",
    description = "Получение прогнозов успеваемости. Предоставляет прогнозы по дедлайнам и уровню риска " +
                  "невыполнения задания на основе анализа затраченного времени студентом. " +
                  "Требует аутентификации и прав доступа PREDICTIONS:READ."
)
@PreAuthorize("hasAuthority('PREDICTIONS:READ')")
@RequiredArgsConstructor
public class PredictionsController {
    private final PredictionService predictionService;

    /**
     * Получает прогноз успеваемости студента по указанному предмету.
     * Анализирует историю трекинга времени и предоставляет прогноз о оставшемся времени
     * и уровне риска невыполнения задания.
     * 
     * Требует прав доступа PREDICTIONS:READ.
     * 
     * @param subject название предмета (например, "ИВТ", "Математика")
     * @return объект прогноза с оставшимся временем и уровнем риска с HTTP статусом 200 (OK)
     */
    @GetMapping("/deadline")
    @Operation(
        summary = "Получить прогноз по дедлайну предмета",
        description = "Возвращает прогноз успеваемости по указанному предмету, включая оставшееся время " +
                      "на выполнение и уровень риска (LOW, MEDIUM, HIGH) на основе анализа затраченного времени. " +
                      "Требует аутентификации и прав доступа PREDICTIONS:READ."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Прогноз успешно получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DeadlinePredictionDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Предмет не найден или некорректное название"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Недостаточно прав доступа"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не аутентифицирован"
        )
    })
    public ResponseEntity<DeadlinePredictionDTO> getDeadlinePrediction(
            @Parameter(
                name = "subject",
                description = "Название предмета для получения прогноза (например, ИВТ, Математика, История)",
                example = "ИВТ",
                required = true
            )
            @RequestParam String subject) {
                
        DeadlinePredictionDTO prediction = predictionService.getPredictionForSubject(subject);
        return ResponseEntity.status(HttpStatus.OK).body(prediction);
    }
}
