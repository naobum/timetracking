package com.kfu.timetracking.services;

import com.kfu.timetracking.models.User;
import com.kfu.timetracking.repositories.UserRepository;
import com.kfu.timetracking.requests.auth.LoginRequest;
import com.kfu.timetracking.requests.auth.RegisterRequest;
import com.kfu.timetracking.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Пользователь уже существует");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        
        userRepository.save(user);
    }

    public Cookie login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        return jwtUtil.generateCookie(user.getUsername(), user.getRole().name());
    }

    public Cookie logout() {
        return jwtUtil.getDeleteCookie();
    }
}
