package com.kfu.timetracking.responses.auth;

import jakarta.servlet.http.Cookie;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Ответ с JWT токенами для аутентифицированного пользователя.
 * Содержит access и refresh токены, которые передаются в cookies.
 */
@Data
@AllArgsConstructor
@Schema(description = "Ответ с JWT токенами после успешной аутентификации")
public class TokenResponse {
    
    @Schema(
        description = "Access токен для аутентификации в запросах (передается в заголовке Authorization или cookie)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        required = true
    )
    private String accessToken;
    
    @Schema(
        description = "Refresh токен для обновления access токена (передается в cookie)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        required = true
    )
    private String refreshToken;
    
    @Schema(
        description = "HTTP cookie с access токеном (устанавливается автоматически)",
        required = false,
        hidden = true
    )
    private Cookie accessCookie;
    
    @Schema(
        description = "HTTP cookie с refresh токеном (устанавливается автоматически)",
        required = false,
        hidden = true
    )
    private Cookie refreshCookie;
}
