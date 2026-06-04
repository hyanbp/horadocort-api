package com.horadocort.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID barberId,
        @NotNull UUID serviceId,
        @NotNull LocalDate date,
        @NotNull LocalTime time,
        @NotBlank String customerName,
        @NotBlank @Pattern(regexp = "\\+?\\d{10,15}") String customerPhone
) {
}
