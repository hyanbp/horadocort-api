package com.horadocort.application.dto;

import java.util.UUID;

public record OnboardingResponse(
        UUID tenantId,
        String slug,
        String bookingUrl,
        String token
) {
}
