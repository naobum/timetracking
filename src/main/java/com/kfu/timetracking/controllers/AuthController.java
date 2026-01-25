package com.kfu.timetracking.controllers;

import com.kfu.timetracking.requests.auth.LoginRequest;
import com.kfu.timetracking.requests.auth.RegisterRequest;
import com.kfu.timetracking.responses.auth.TokenResponse;
import com.kfu.timetracking.security.JwtUtil;
import com.kfu.timetracking.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Аутентификация и регистрация")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("Пользователь успешно зарегистрирован");
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему")
    public ResponseEntity<String> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        TokenResponse tokenResponse = authService.login(request);
        
        if (tokenResponse.getAccessCookie() != null) {
            response.addCookie(tokenResponse.getAccessCookie());
        }
        if (tokenResponse.getRefreshCookie() != null) {
            response.addCookie(tokenResponse.getRefreshCookie());
        }
        
        return ResponseEntity.ok("Успешный вход");
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Обновление access токена")
    public ResponseEntity<String> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh токен не найден");
        }
        
        TokenResponse tokenResponse = authService.refreshToken(refreshToken);
        
        if (tokenResponse.getAccessCookie() != null) {
            response.addCookie(tokenResponse.getAccessCookie());
        }
        
        return ResponseEntity.ok("Токен обновлен");
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход из системы")
    public ResponseEntity<String> logout(
            Authentication authentication,
            HttpServletResponse response) {
        
        if (authentication != null) {
            authService.logout(authentication.getName());
        }
        
        response.addCookie(new JwtUtil().getDeleteCookie(JwtUtil.ACCESS_COOKIE_NAME));
        response.addCookie(new JwtUtil().getDeleteCookie(JwtUtil.REFRESH_COOKIE_NAME));
        
        return ResponseEntity.ok("Успешный выход");
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
