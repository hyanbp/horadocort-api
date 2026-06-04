package com.horadocort.application.dto;

import java.time.LocalTime;
import java.util.List;

public record AvailabilityResponse(
        List<TimeSlot> slots
) {
    public record TimeSlot(LocalTime time, boolean available) {
    }
}
