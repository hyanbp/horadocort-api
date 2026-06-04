package com.horadocort.application.dto;

public record PublicTenantResponse(
        String slug,
        String name,
        int openingHour,
        int closingHour,
        String timezone
) {
}
