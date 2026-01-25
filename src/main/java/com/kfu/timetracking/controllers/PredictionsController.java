package com.kfu.timetracking.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kfu.timetracking.responses.predictions.DeadlinePredictionDTO;
import com.kfu.timetracking.services.PredictionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/predictions")
@Tag(name = "Predictions", description = "Получение прогноза по дедлайнам")
 @PreAuthorize("hasAuthority('PREDICTIONS:READ')")
@RequiredArgsConstructor
public class PredictionsController {
    private final PredictionService predictionService;

    /**
     * GET /api/predictions/deadline?subject=ИВТ
     * Получает прогноз успеваемости по предмету.
     *
     * @param subject Название предмета (e.g., "ИВТ")
     * @return ResponseEntity с DeadlinePredictionDTO 
     */
    @GetMapping("/deadline")
    @Operation(summary = "Прогноз успеваемости по предмету", 
    description = "Получает прогноз успеваемости по предмету: оставшееся время и статус риска")
    public ResponseEntity<DeadlinePredictionDTO> getDeadlinePrediction(
            @RequestParam String subject) {
                
        DeadlinePredictionDTO prediction = predictionService.getPredictionForSubject(subject);
        return ResponseEntity.ok(prediction);
    }
}
