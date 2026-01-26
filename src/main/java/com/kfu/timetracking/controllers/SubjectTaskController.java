package com.kfu.timetracking.controllers;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kfu.timetracking.services.SubjectTaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/subject-tasks")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Subject Tasks", description = "Управление задачами по предметам")
public class SubjectTaskController {
    private final SubjectTaskService subjectTaskService;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузить CSV файл с задачами", description = "Загружает CSV файл с данными: task_type,subject,expectedHours", responses = {
        @ApiResponse(responseCode = "200", description = "Файл успешно загружен и обработан"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации файла"),
        @ApiResponse(responseCode = "500", description = "Ошибка при обработке файла")
    })
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл не может быть пустым");
        }
        
        if (!file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body("Файл должен быть в формате CSV");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body("Размер файла превышает допустимый лимит (5 MB)");
        }
        
        try {
            subjectTaskService.uploadSubjectTasksFromCsv(file);
            return ResponseEntity.ok("Файл успешно загружен и обработан");
        } catch (IOException e) {
            log.error("Ошибка при загрузке файла", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при обработке файла: " + e.getMessage());
        }
    }
}
