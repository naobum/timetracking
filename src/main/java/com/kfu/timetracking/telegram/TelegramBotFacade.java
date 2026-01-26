package com.kfu.timetracking.telegram;

import com.kfu.timetracking.services.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Фасад для работы с Telegram ботом
 * Предоставляет удобный интерфейс для логирования различных событий в приложении
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true")
public class TelegramBotFacade {

    private final TelegramNotificationService telegramService;

    /**
     * Логирует успешный вход пользователя
     */
    public void logLoginSuccess(String username) {
        try {
            telegramService.logAuthEvent("LOGIN", username, "SUCCESS");
            log.info("Логирована успешная аутентификация пользователя: {}", username);
        } catch (Exception e) {
            log.error("Ошибка при логировании входа: {}", e.getMessage());
        }
    }

    /**
     * Логирует неудачный вход пользователя
     */
    public void logLoginFailure(String username) {
        try {
            telegramService.logAuthEvent("LOGIN", username, "FAILED");
            log.warn("Логирована неудачная попытка входа пользователя: {}", username);
        } catch (Exception e) {
            log.error("Ошибка при логировании неудачного входа: {}", e.getMessage());
        }
    }

    /**
     * Логирует регистрацию пользователя
     */
    public void logRegistration(String username) {
        try {
            telegramService.logAuthEvent("REGISTER", username, "SUCCESS");
            log.info("Логирована регистрация пользователя: {}", username);
        } catch (Exception e) {
            log.error("Ошибка при логировании регистрации: {}", e.getMessage());
        }
    }

    /**
     * Логирует выход пользователя
     */
    public void logLogout(String username) {
        try {
            telegramService.logAuthEvent("LOGOUT", username, "SUCCESS");
            log.info("Логирован выход пользователя: {}", username);
        } catch (Exception e) {
            log.error("Ошибка при логировании выхода: {}", e.getMessage());
        }
    }

    /**
     * Логирует обновление токена
     */
    public void logTokenRefresh(String username) {
        try {
            telegramService.logAuthEvent("TOKEN_REFRESH", username, "SUCCESS");
            log.info("Логировано обновление токена для пользователя: {}", username);
        } catch (Exception e) {
            log.error("Ошибка при логировании обновления токена: {}", e.getMessage());
        }
    }
}
