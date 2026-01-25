package com.kfu.timetracking.security;

import com.kfu.timetracking.models.Token;
import com.kfu.timetracking.models.TokenType;
import com.kfu.timetracking.repositories.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        log.debug("Обработка JWT аутентификации для URL: {}", request.getRequestURI());
        Optional<String> jwtToken = extractJwtFromCookie(request, JwtUtil.ACCESS_COOKIE_NAME);
        
        if (jwtToken.isPresent()) {
            String token = jwtToken.get();
            String username = jwtUtil.extractUsername(token);
            log.debug("Найден токен для пользователя: {}", username);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Проверяем токен в БД
                Optional<Token> tokenEntity = tokenRepository.findByValueAndType(token, TokenType.ACCESS);
                
                if (jwtUtil.isTokenValid(token, username) && 
                    tokenEntity.isPresent() && 
                    tokenEntity.get().isValid()) {
                    
                    log.info("JWT токен валиден и установлена аутентификация для пользователя: {}", username);
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("JWT токен невалиден для пользователя: {}", username);
                }
            }
        } else {
            log.debug("JWT токен не найден в cookies для URL: {}", request.getRequestURI());
        }
        
        filterChain.doFilter(request, response);
    }

    private Optional<String> extractJwtFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
