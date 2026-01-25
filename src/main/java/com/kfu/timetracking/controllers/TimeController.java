package com.kfu.timetracking.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.kfu.timetracking.requests.timeTrack.StartTimeTrackRequest;
import com.kfu.timetracking.requests.timeTrack.StopTimeTrackRequest;
import com.kfu.timetracking.responses.time.TimeEntryDto;
import com.kfu.timetracking.responses.time.TimeStartedResponse;
import com.kfu.timetracking.responses.time.TimeStoppedResponse;
import com.kfu.timetracking.services.TimeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST контроллер для управления трекингом времени студентов.
 * Позволяет запускать и останавливать отслеживание времени, 
 * а также получать статистику по затраченному времени.
 */
@RestController
@RequestMapping("/api/time")
@Tag(
    name = "TimeEntries",
    description = "Управление трекингом времени. Позволяет начинать и завершать отслеживание " +
                  "времени учебной деятельности студентов, а также получать статистику затраченного времени."
)
public class TimeController {
    private final TimeService timeService;

    public TimeController(TimeService timeService){
        this.timeService = timeService;
    }

    /**
     * Начинает отслеживание времени для студента.
     * Требует прав доступа TIME:WRITE.
     * 
     * @param request данные о начале трекинга (ID студента, предмет)
     * @return информация о начатом трекинге с HTTP статусом 201 (Created)
     */
    @PostMapping("/start")
    @PreAuthorize("hasAuthority('TIME:WRITE')")
    @Operation(
        summary = "Начать отслеживание времени",
        description = "Запускает таймер для отслеживания времени учебной деятельности студента. " +
                      "Требует прав доступа TIME:WRITE."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Трекинг успешно запущен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TimeStartedResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные или студент не найден"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Недостаточно прав доступа"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Ошибка валидации входных данных"
        )
    })
    public ResponseEntity<TimeStartedResponse> start(@RequestBody StartTimeTrackRequest request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(timeService.start(request));
    }
    
    /**
     * Завершает отслеживание времени для студента.
     * Требует прав доступа TIME:WRITE.
     * 
     * @param request данные о завершении трекинга (ID студента, время окончания)
     * @return информация о завершенном трекинге с HTTP статусом 200 (OK)
     */
    @PostMapping("/stop")
    @PreAuthorize("hasAuthority('TIME:WRITE')")
    @Operation(
        summary = "Завершить отслеживание времени",
        description = "Останавливает таймер и сохраняет затраченное время. " +
                      "Требует прав доступа TIME:WRITE."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Трекинг успешно завершен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TimeStoppedResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные или активный трекинг не найден"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Недостаточно прав доступа"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Ошибка валидации входных данных"
        )
    })
    public ResponseEntity<TimeStoppedResponse> stop(@RequestBody StopTimeTrackRequest request) {
        
        return ResponseEntity.status(HttpStatus.OK)
            .body(timeService.stop(request));
    }

    /**
     * Получает статистику времени за последнюю неделю для указанного студента.
     * Требует прав доступа TIME:READ.
     * 
     * @param studentId уникальный идентификатор студента
     * @return список записей о затраченном времени за неделю с HTTP статусом 200 (OK)
     */
    @GetMapping("/weekly")
    @PreAuthorize("hasAuthority('TIME:READ')")
    @Operation(
        summary = "Получить недельную статистику времени",
        description = "Возвращает список всех записей о затраченном времени студентом за последние 7 дней. " +
                      "Требует прав доступа TIME:READ."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Статистика успешно получена",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = TimeEntryDto.class))
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Недостаточно прав доступа"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Студент не найден"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректный ID студента"
        )
    })
    public ResponseEntity<List<TimeEntryDto>> getWeeklyReport(@RequestParam Long studentId) {
        List<TimeEntryDto> entries = timeService.getWeeklyStats(studentId);
        return ResponseEntity.ok(entries);
    }
}
