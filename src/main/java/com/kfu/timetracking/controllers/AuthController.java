package com.kfu.timetracking.controllers;

import com.kfu.timetracking.requests.auth.LoginRequest;
import com.kfu.timetracking.requests.auth.RegisterRequest;
import com.kfu.timetracking.responses.auth.TokenResponse;
import com.kfu.timetracking.security.JwtUtil;
import com.kfu.timetracking.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * REST контроллер для управления аутентификацией и авторизацией пользователей.
 * Обеспечивает функциональность регистрации, входа, обновления токенов и выхода.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(
    name = "Authentication",
    description = "Управление аутентификацией и авторизацией пользователей. " +
                  "Включает операции регистрации, входа в систему, обновления токенов доступа и выхода."
)
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;

    /**
     * Регистрирует нового пользователя в системе.
     * 
     * @param request данные для регистрации (имя пользователя, пароль, роль)
     * @return сообщение об успешной регистрации
     */
    @PostMapping("/register")
    @Operation(
        summary = "Регистрация нового пользователя",
        description = "Создает новый аккаунт пользователя с указанными учетными данными и ролью"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Пользователь успешно зарегистрирован",
            content = @Content(mediaType = "application/json", schema = @Schema(example = "Пользователь успешно зарегистрирован"))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные регистрации (например, пользователь уже существует)"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Ошибка валидации входных данных"
        )
    })
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest request) {
        log.debug("POST /api/auth/register - запрос регистрации от {}", request.getUsername());
        try {
            authService.register(request);
            log.info("Успешная регистрация пользователя: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body("Пользователь успешно зарегистрирован");
        } catch (Exception e) {
            log.error("Ошибка при регистрации пользователя: {}", request.getUsername(), e);
            throw e;
        }
    }

    /**
     * Аутентифицирует пользователя и выдает JWT токены.
     * Токены передаются в виде HTTP-only cookies для безопасности.
     * 
     * @param request учетные данные пользователя (имя пользователя и пароль)
     * @param response объект для установки cookies
     * @return сообщение об успешном входе
     */
    @PostMapping("/login")
    @Operation(
        summary = "Вход в систему (аутентификация)",
        description = "Проверяет учетные данные пользователя и выдает access и refresh JWT токены. " +
                      "Токены устанавливаются в HTTP-only cookies для защиты от XSS атак."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Успешная аутентификация, токены установлены в cookies",
            content = @Content(mediaType = "application/json", schema = @Schema(example = "Успешный вход"))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Неверные учетные данные или пользователь не найден"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Ошибка валидации входных данных"
        )
    })
    public ResponseEntity<String> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        log.debug("POST /api/auth/login - запрос входа от {}", request.getUsername());
        try {
            TokenResponse tokenResponse = authService.login(request);
            
            if (tokenResponse.getAccessCookie() != null) {
                response.addCookie(tokenResponse.getAccessCookie());
            }
            if (tokenResponse.getRefreshCookie() != null) {
                response.addCookie(tokenResponse.getRefreshCookie());
            }
            
            log.info("Успешный вход пользователя: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body("Успешный вход");
        } catch (Exception e) {
            log.warn("Ошибка при входе пользователя: {}", request.getUsername());
            throw e;
        }
    }
    
    /**
     * Обновляет истекший access токен, используя refresh токен.
     * Refresh токен передается через cookie.
     * 
     * @param request HTTP запрос с refresh токеном в cookie
     * @param response объект для установки нового access токена
     * @return сообщение об успешном обновлении токена
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Обновление access токена",
        description = "Использует refresh токен (из cookie) для получения нового access токена. " +
                      "Новый access токен устанавливается в cookie."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Access токен успешно обновлен",
            content = @Content(mediaType = "application/json", schema = @Schema(example = "Токен обновлен"))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Refresh токен не найден или невалиден"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Refresh токен истек или недействителен"
        )
    })
    public ResponseEntity<String> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        log.debug("POST /api/auth/refresh - запрос обновления токена");
        String refreshToken = extractRefreshTokenFromCookie(request);
        
        if (refreshToken == null) {
            log.warn("Refresh токен не найден в cookies");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh токен не найден");
        }
        
        try {
            TokenResponse tokenResponse = authService.refreshToken(refreshToken);
            
            if (tokenResponse.getAccessCookie() != null) {
                response.addCookie(tokenResponse.getAccessCookie());
            }
            
            log.info("Токен успешно обновлен");
            return ResponseEntity.status(HttpStatus.OK).body("Токен обновлен");
        } catch (Exception e) {
            log.warn("Ошибка при обновлении токена: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Выполняет выход пользователя из системы.
     * Удаляет access и refresh токены, инвалидируя текущую сессию.
     * 
     * @param authentication текущий аутентифицированный пользователь
     * @param response объект для удаления cookies
     * @return сообщение об успешном выходе
     */
    @PostMapping("/logout")
    @Operation(
        summary = "Выход из системы",
        description = "Инвалидирует текущую сессию пользователя путем удаления JWT токенов. " +
                      "Требует аутентификации."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Пользователь успешно вышел из системы"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не аутентифицирован"
        )
    })
    public ResponseEntity<Void> logout(
            Authentication authentication,
            HttpServletResponse response) {
        log.debug("POST /api/auth/logout - запрос выхода");
        
        if (authentication != null) {
            log.info("Выход пользователя: {}", authentication.getName());
            authService.logout(authentication.getName());
        } else {
            log.warn("Попытка выхода без аутентификации");
        }
        
        response.addCookie(new JwtUtil().getDeleteCookie(JwtUtil.ACCESS_COOKIE_NAME));
        response.addCookie(new JwtUtil().getDeleteCookie(JwtUtil.REFRESH_COOKIE_NAME));
        
        log.info("Пользователь успешно вышел из системы");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        
        return Arrays.stream(cookies)
                .filter(cookie -> JwtUtil.REFRESH_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
