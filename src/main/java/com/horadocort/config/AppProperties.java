package com.horadocort.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Defaults defaults
) {
    public record Jwt(String secret, int expirationHours) {
    }

    public record Defaults(
            int openingHour,
            int closingHour,
            int slotDurationMinutes,
            int reminderWindowMinutes
    ) {
    }
}
