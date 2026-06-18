package com.horadocort.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({AppProperties.class, RelayOsProperties.class})
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebClient relayOsWebClient(RelayOsProperties properties) {
        validateRelayOsConfig(properties);
        return WebClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    private void validateRelayOsConfig(RelayOsProperties properties) {
        if (properties.baseUrl() == null || !properties.baseUrl().startsWith("https://")) {
            throw new IllegalStateException(
                    "RELAYOS_BASE_URL inválida ou não configurada: " + properties.baseUrl());
        }
        if (properties.apiKey() == null
                || properties.apiKey().isBlank()
                || "desabilitado".equalsIgnoreCase(properties.apiKey())) {
            throw new IllegalStateException(
                    "RELAYOS_API_KEY não configurada (valor atual rejeitado por motivo de segurança)");
        }
    }
}