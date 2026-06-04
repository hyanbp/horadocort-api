package com.horadocort.application.dto;

import jakarta.validation.constraints.*;

public record OnboardingRequest(
        @NotBlank @Size(min = 3, max = 60) @Pattern(regexp = "^[a-z0-9][a-z0-9-]*$",
                message = "Slug deve usar apenas letras minúsculas, números e hífens") String slug,
        @NotBlank @Size(max = 120) String barbershopName,
        @NotBlank @Email String ownerEmail,
        @NotBlank @Size(min = 8, max = 100) String ownerPassword,
        @NotBlank @Size(max = 120) String ownerName,
        @Pattern(regexp = "\\+?\\d{10,15}") String ownerPhone
) {
}
