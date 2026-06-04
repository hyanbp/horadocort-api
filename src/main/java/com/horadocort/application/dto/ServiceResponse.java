package com.horadocort.application.dto;

import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String name,
        int durationMinutes,
        int priceCents
) {
}
