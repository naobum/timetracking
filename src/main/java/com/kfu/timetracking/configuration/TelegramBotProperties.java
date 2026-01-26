package com.kfu.timetracking.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Свойства конфигурации для Telegram бота
 */
@Data
@Component
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotProperties {
    
    /**
     * Имя бота
     */
    private String name;
    
    /**
     * Токен Telegram бота
     */
    private String token;
    
    /**
     * ID чата для отправки логов
     */
    private String chatId;
    
    /**
     * Включен ли функционал
     */
    private boolean enabled = false;
    
    /**
     * Логировать ли все запросы
     */
    private boolean logAllRequests = true;
    
    /**
     * Исключать ли публичные endpoints
     */
    private boolean filterPublicEndpoints = true;
}
