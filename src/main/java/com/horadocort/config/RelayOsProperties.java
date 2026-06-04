package com.horadocort.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "relayos")
public record RelayOsProperties(
        String baseUrl,
        String apiKey,
        Templates templates
) {
    public record Templates(
            String bookingConfirmed,
            String bookingReminder,
            String barberNotification
    ) {
    }
}
