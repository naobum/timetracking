package com.kfu.timetracking.responses.students;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для передачи информации о студенте.
 * Используется в ответах API.
 */
@Data
@AllArgsConstructor
@Schema(description = "Информация о студенте")
public class StudentDto {
    
    @Schema(
        description = "Уникальный идентификатор студента",
        example = "1",
        required = true
    )
    private Long id;
    
    @Schema(
        description = "Полное имя студента",
        example = "Иван Петров",
        required = true
    )
    private String name;
    
    @Schema(
        description = "Название учебной группы студента",
        example = "ПМ-101",
        required = true
    )
    private String groupName;
}
