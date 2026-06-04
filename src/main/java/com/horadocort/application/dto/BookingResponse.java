package com.horadocort.application.dto;

import com.horadocort.domain.entity.BookingStatus;

import java.time.ZonedDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID barberId,
        String barberName,
        UUID serviceId,
        String serviceName,
        String customerName,
        String customerPhone,
        ZonedDateTime startAt,
        ZonedDateTime endAt,
        BookingStatus status
) {
}
