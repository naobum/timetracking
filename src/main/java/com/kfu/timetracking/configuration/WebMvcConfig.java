package com.kfu.timetracking.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

/**
 * Конфигурация веб-приложения для регистрации перехватчиков
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final Optional<TelegramLoggingInterceptor> telegramLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        telegramLoggingInterceptor.ifPresent(interceptor ->
            registry.addInterceptor(interceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/static/**",
                    "/*.css",
                    "/*.js",
                    "/*.png",
                    "/*.jpg",
                    "/*.gif"
                )
        );
    }
}
