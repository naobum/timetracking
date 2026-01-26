package com.kfu.timetracking.configuration;

import com.kfu.timetracking.services.TelegramNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Перехватчик для логирования входящих HTTP запросов через Telegram
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true")
public class TelegramLoggingInterceptor implements HandlerInterceptor {

    private final TelegramNotificationService telegramService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // Проверяем, нужно ли логировать этот запрос
            if (shouldLogRequest(request)) {
                String method = request.getMethod();
                String uri = request.getRequestURI();
                String remoteAddr = getClientIP(request);
                String userAgent = request.getHeader("User-Agent");

                telegramService.logIncomingRequest(method, uri, remoteAddr, userAgent);
            }
        } catch (Exception e) {
            log.error("Ошибка в TelegramLoggingInterceptor.preHandle", e);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            // Логируем ошибки (статус коды 4xx и 5xx)
            int statusCode = response.getStatus();
            if (statusCode >= 400 && shouldLogRequest(request)) {
                String method = request.getMethod();
                String uri = request.getRequestURI();
                String errorMessage = ex != null ? ex.getMessage() : "Неизвестная ошибка";

                telegramService.logErrorRequest(method, uri, statusCode, errorMessage);
            }
        } catch (Exception e) {
            log.error("Ошибка в TelegramLoggingInterceptor.afterCompletion", e);
        }
    }

    /**
     * Определяет, нужно ли логировать запрос
     */
    private boolean shouldLogRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        
        // Исключаем публичные endpoints (Swagger, статические файлы и т.д.)
        return !uri.startsWith("/v3/api-docs") &&
               !uri.startsWith("/swagger-ui") &&
               !uri.startsWith("/static/") &&
               !uri.endsWith(".css") &&
               !uri.endsWith(".js") &&
               !uri.endsWith(".png") &&
               !uri.endsWith(".jpg") &&
               !uri.endsWith(".gif") &&
               !uri.endsWith(".woff") &&
               !uri.endsWith(".woff2");
    }

    /**
     * Получает IP адрес клиента
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
