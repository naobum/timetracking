package com.kfu.timetracking.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI (Swagger) документации для TimeTracking API.
 * 
 * Автоматически генерирует интерактивную документацию API.
 * Доступна по адресу: http://localhost:8080/swagger-ui.html
 * JSON спецификация: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    /**
     * Создает конфигурацию OpenAPI с метаинформацией о проекте.
     * 
     * @return объект OpenAPI с полной документацией API
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createInfo())
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Development Server"))
                .addServersItem(new Server()
                        .url("https://api.example.com")
                        .description("Production Server"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                .addSecurityItem(new SecurityRequirement().addList("Cookie Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Token",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Bearer token полученный при входе. " +
                                                "Используется в заголовке Authorization: Bearer <token>"))
                        .addSecuritySchemes("Cookie Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("access_token")
                                        .description("JWT token передается в HTTP-only cookie для защиты от XSS атак")));
    }

    /**
     * Создает информацию о API.
     * 
     * @return объект Info с деталями проекта
     */
    private Info createInfo() {
        return new Info()
                .title("TimeTracking API")
                .version("1.0.0")
                .description(
                        "REST API для управления трекингом времени учебной деятельности студентов. " +
                                "Позволяет регистрировать, аутентифицировать пользователей, отслеживать " +
                                "затраченное время на различные типы деятельности и получать прогнозы по успеваемости.\n\n" +
                                "## Основные возможности:\n\n" +
                                "### Аутентификация и авторизация\n" +
                                "- Регистрация новых пользователей\n" +
                                "- Вход в систему с получением JWT токенов\n" +
                                "- Обновление access токена\n" +
                                "- Безопасный выход из системы\n\n" +
                                "### Управление студентами\n" +
                                "- Добавление студентов в систему\n" +
                                "- Получение информации о студентах\n" +
                                "- Удаление студентов из системы\n\n" +
                                "### Трекинг времени\n" +
                                "- Запуск таймера для отслеживания времени\n" +
                                "- Остановка таймера и сохранение данных\n" +
                                "- Получение статистики затраченного времени за неделю\n\n" +
                                "### Прогнозирование\n" +
                                "- Получение прогноза по дедлайнам предметов\n" +
                                "- Оценка уровня риска невыполнения заданий\n\n" +
                                "## Безопасность\n\n" +
                                "API использует JWT (JSON Web Tokens) для аутентификации. Токены передаются " +
                                "в виде HTTP-only cookies для защиты от XSS атак. Каждый запрос требует валидного " +
                                "access токена в заголовке Authorization или в cookie.\n\n" +
                                "## Статус кодов\n\n" +
                                "- **200 OK** - успешный запрос\n" +
                                "- **201 Created** - ресурс успешно создан\n" +
                                "- **204 No Content** - успешная операция без содержимого в ответе\n" +
                                "- **400 Bad Request** - неверные входные данные\n" +
                                "- **401 Unauthorized** - отсутствует аутентификация\n" +
                                "- **403 Forbidden** - недостаточно прав доступа\n" +
                                "- **404 Not Found** - ресурс не найден\n" +
                                "- **422 Unprocessable Entity** - ошибка валидации\n" +
                                "- **500 Internal Server Error** - внутренняя ошибка сервера\n"
                );
    }
}
