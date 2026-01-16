package com.kfu.timetracking.controllers;

import com.kfu.timetracking.requests.auth.LoginRequest;
import com.kfu.timetracking.requests.auth.RegisterRequest;
import com.kfu.timetracking.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        Cookie jwtCookie = authService.login(request);
        response.addCookie(jwtCookie);
        return ResponseEntity.ok("Успешный вход");
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход из системы")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        Cookie deleteCookie = authService.logout();
        response.addCookie(deleteCookie);
        return ResponseEntity.ok("Успешный выход");
    }
}
