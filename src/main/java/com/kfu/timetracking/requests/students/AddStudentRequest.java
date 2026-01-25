package com.kfu.timetracking.requests.students;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Запрос для добавления нового студента.
 * Содержит основную информацию о студенте.
 */
@Data
@Schema(
    description = "Запрос для добавления нового студента в систему",
    example = "{\"name\": \"Иван Петров\", \"groupName\": \"ПМ-101\"}"
)
public class AddStudentRequest {
    
    @NotBlank(message = "Имя студента не может быть пустым")
    @Schema(
        description = "Полное имя студента",
        example = "Иван Петров",
        required = true
    )
    public String name;
    
    @NotBlank(message = "Название группы не может быть пустым")
    @Schema(
        description = "Название учебной группы",
        example = "ПМ-101",
        required = true
    )
    public String groupName;
}
