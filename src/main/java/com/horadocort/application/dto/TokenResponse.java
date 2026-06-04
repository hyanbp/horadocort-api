package com.horadocort.application.dto;

import java.util.UUID;

public record TokenResponse(
        String token,
        UUID tenantId,
        String tenantSlug,
        String tenantName,
        String userName,
        String role
) {
}
