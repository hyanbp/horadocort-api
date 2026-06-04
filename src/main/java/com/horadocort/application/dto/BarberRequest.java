package com.horadocort.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BarberRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "\\+?\\d{10,15}") String phone,
        String avatarUrl
) {
}
