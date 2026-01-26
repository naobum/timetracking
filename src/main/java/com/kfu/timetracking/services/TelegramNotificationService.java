package com.kfu.timetracking.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для отправки уведомлений через Telegram бота
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true")
@RequiredArgsConstructor
public class TelegramNotificationService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    @Value("${telegram.bot.name}")
    private String botName;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Отправляет логи о входящем HTTP запросе
     */
    public void logIncomingRequest(String method, String uri, String remoteAddr, String userAgent) {
        try {
            String message = formatRequestMessage(method, uri, remoteAddr, userAgent);
            sendMessage(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке логов запроса в Telegram", e);
        }
    }

    /**
     * Отправляет логи об ошибке запроса
     */
    public void logErrorRequest(String method, String uri, int statusCode, String errorMessage) {
        try {
            String message = formatErrorMessage(method, uri, statusCode, errorMessage);
            sendMessage(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке логов ошибки в Telegram", e);
        }
    }

    /**
     * Отправляет логи об аутентификации
     */
    public void logAuthEvent(String eventType, String username, String result) {
        try {
            String message = formatAuthMessage(eventType, username, result);
            sendMessage(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке логов аутентификации в Telegram", e);
        }
    }

    /**
     * Отправляет сообщение в Telegram
     */
    private void sendMessage(String messageText) {
        try {
            String url = TELEGRAM_API_URL + botToken + "/sendMessage";
            
            // Создаем payload для Telegram API
            Map<String, String> payload = new HashMap<>();
            payload.put("chat_id", chatId);
            payload.put("text", messageText);
            payload.put("parse_mode", "HTML");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(payload),
                headers
            );

            restTemplate.postForObject(url, request, String.class);
            
        } catch (RestClientException e) {
            log.warn("Не удалось отправить сообщение в Telegram: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Ошибка при отправке сообщения в Telegram: {}", e.getMessage());
        }
    }

    /**
     * Форматирует сообщение о входящем запросе
     */
    private String formatRequestMessage(String method, String uri, String remoteAddr, String userAgent) {
        return String.format(
            "<b>📨 Входящий запрос</b>\n" +
            "⏰ Время: %s\n" +
            "🔹 Метод: <code>%s</code>\n" +
            "📍 URI: <code>%s</code>\n" +
            "🌐 IP: <code>%s</code>\n" +
            "🔗 User-Agent: <code>%s</code>",
            LocalDateTime.now().format(FORMATTER),
            method,
            uri,
            remoteAddr,
            userAgent != null ? userAgent : "unknown"
        );
    }

    /**
     * Форматирует сообщение об ошибке запроса
     */
    private String formatErrorMessage(String method, String uri, int statusCode, String errorMessage) {
        String statusEmoji = getStatusEmoji(statusCode);
        return String.format(
            "<b>❌ Ошибка запроса</b>\n" +
            "⏰ Время: %s\n" +
            "🔹 Метод: <code>%s</code>\n" +
            "📍 URI: <code>%s</code>\n" +
            "%s Статус: <b>%d</b>\n" +
            "💬 Сообщение: <code>%s</code>",
            LocalDateTime.now().format(FORMATTER),
            method,
            uri,
            statusEmoji,
            statusCode,
            errorMessage
        );
    }

    /**
     * Форматирует сообщение об аутентификации
     */
    private String formatAuthMessage(String eventType, String username, String result) {
        String emoji = "SUCCESS".equals(result) ? "✅" : "❌";
        return String.format(
            "<b>🔐 Событие аутентификации</b>\n" +
            "⏰ Время: %s\n" +
            "📌 Тип события: <code>%s</code>\n" +
            "👤 Пользователь: <code>%s</code>\n" +
            "%s Результат: <b>%s</b>",
            LocalDateTime.now().format(FORMATTER),
            eventType,
            username,
            emoji,
            result
        );
    }

    /**
     * Получает эмодзи для статус-кода
     */
    private String getStatusEmoji(int statusCode) {
        if (statusCode >= 500) return "🔴";
        if (statusCode >= 400) return "🟠";
        if (statusCode >= 300) return "🟡";
        if (statusCode >= 200) return "🟢";
        return "⚪";
    }
}
