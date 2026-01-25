package com.kfu.timetracking.services;

import com.kfu.timetracking.models.Role;
import com.kfu.timetracking.models.Token;
import com.kfu.timetracking.models.TokenType;
import com.kfu.timetracking.models.User;
import com.kfu.timetracking.repositories.RoleRepository;
import com.kfu.timetracking.repositories.TokenRepository;
import com.kfu.timetracking.repositories.UserRepository;
import com.kfu.timetracking.requests.auth.LoginRequest;
import com.kfu.timetracking.requests.auth.RegisterRequest;
import com.kfu.timetracking.responses.auth.TokenResponse;
import com.kfu.timetracking.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void register(RegisterRequest request) {
        log.info("Попытка регистрации пользователя: {}", request.getUsername());
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Попытка регистрации существующего пользователя: {}", request.getUsername());
            throw new IllegalArgumentException("Пользователь уже существует");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        Role role = roleRepository.findByName(request.getRole().name())
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        userRepository.save(user);
        log.info("Пользователь успешно зарегистрирован: {} с ролью: {}", request.getUsername(), request.getRole());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("Попытка входа пользователя: {}", request.getUsername());
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
        } catch (Exception e) {
            log.warn("Ошибка аутентификации для пользователя: {}", request.getUsername());
            throw e;
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Деактивируем старые токены
        tokenRepository.disableAllUserTokensByType(user, TokenType.ACCESS);
        tokenRepository.disableAllUserTokensByType(user, TokenType.REFRESH);
        
        String roleName = user.getRoles().iterator().next().getName();
        
        // Генерируем новые токены
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), roleName);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), roleName);
        
        // Сохраняем токены в БД
        saveToken(user, accessToken, TokenType.ACCESS);
        saveToken(user, refreshToken, TokenType.REFRESH);

        Cookie accessCookie = jwtUtil.generateAccessCookie(user.getUsername(), roleName);
        Cookie refreshCookie = jwtUtil.generateRefreshCookie(user.getUsername(), roleName);
        
        log.info("Пользователь успешно вошел: {}", request.getUsername());
        return new TokenResponse(accessToken, refreshToken, accessCookie, refreshCookie);
    }
    
    @Transactional
    public TokenResponse refreshToken(String refreshTokenValue) {
        log.debug("Попытка обновления токена");
        Token refreshToken = tokenRepository.findByValueAndType(refreshTokenValue, TokenType.REFRESH)
                .orElseThrow(() -> new IllegalArgumentException("Refresh токен не найден"));
        
        if (!refreshToken.isValid()) {
            log.warn("Попытка использования недействительного refresh токена");
            throw new IllegalArgumentException("Refresh токен недействителен");
        }
        
        User user = refreshToken.getUser();
        String roleName = user.getRoles().iterator().next().getName();
        
        // Деактивируем старые access токены
        tokenRepository.disableAllUserTokensByType(user, TokenType.ACCESS);
        
        // Генерируем новый access токен
        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), roleName);
        saveToken(user, newAccessToken, TokenType.ACCESS);
        
        log.info("Токен успешно обновлен для пользователя: {}", user.getUsername());
        Cookie accessCookie = jwtUtil.generateAccessCookie(user.getUsername(), roleName);
        
        return new TokenResponse(newAccessToken, refreshTokenValue, accessCookie, null);
    }

    @Transactional
    public void logout(String username) {
        log.info("Выход пользователя: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        
        tokenRepository.disableAllUserTokensByType(user, TokenType.ACCESS);
        tokenRepository.disableAllUserTokensByType(user, TokenType.REFRESH);
        log.info("Пользователь успешно вышел: {}", username);
    }
    
    private void saveToken(User user, String tokenValue, TokenType type) {
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(
            type == TokenType.ACCESS ? 900 : 604800 // 15 мин или 7 дней
        );
        
        Token token = new Token(type, tokenValue, expiryDate, false, user);
        tokenRepository.save(token);
    }
}
