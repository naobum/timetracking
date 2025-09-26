package com.kfu.timetracking.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // для REST без браузерных форм
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/time/**").permitAll()
                .anyRequest().permitAll()
            )
            .build();
    }
}
