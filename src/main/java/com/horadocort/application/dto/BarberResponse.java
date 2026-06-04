package com.horadocort.application.dto;

import java.util.UUID;

public record BarberResponse(
        UUID id,
        String name,
        String avatarUrl
) {
}
