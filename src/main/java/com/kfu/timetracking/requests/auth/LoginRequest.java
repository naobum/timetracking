package com.kfu.timetracking.requests.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Запрос для входа в систему.
 * Содержит учетные данные пользователя.
 */
@Data
@Schema(
    description = "Запрос для аутентификации в системе",
    example = "{\"username\": \"student1\", \"password\": \"password123\"}"
)
public class LoginRequest {
    
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
}

