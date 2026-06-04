package com.horadocort.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ServiceRequest(
        @NotBlank String name,
        @Min(10) int durationMinutes,
        @Min(0) int priceCents
) {
}
