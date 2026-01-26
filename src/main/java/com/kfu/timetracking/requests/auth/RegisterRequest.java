package com.kfu.timetracking.requests.auth;

import com.kfu.timetracking.requests.auth.enums.RoleEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Запрос для регистрации нового пользователя.
 * Содержит учетные данные и информацию о роли пользователя.
 */
@Data
@Schema(
    description = "Запрос для регистрации нового пользователя",
    example = "{\"username\": \"student1\", \"password\": \"password123\", \"role\": \"STUDENT\", \"studentId\": 1}"
)
public class RegisterRequest {
    
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Schema(
        description = "Имя пользователя (уникальный идентификатор)",
        example = "student1",
        required = true
    )
    private String username;
    
    @NotBlank(message = "Пароль не может быть пустым")
    @Schema(
        description = "Пароль пользователя",
        example = "password123",
        required = true
    )
    private String password;
    
    @NotNull(message = "Роль не может быть пустой")
    @Schema(
        description = "Роль пользователя в системе (ADMIN, STUDENT, INSTRUCTOR)",
        example = "STUDENT",
        required = true,
        implementation = RoleEnum.class
    )
    private RoleEnum role;
    
    @Schema(
        description = "ID студента (обязателен для роли STUDENT)",
        example = "1",
        required = false
    )
    private Long studentId;
}
