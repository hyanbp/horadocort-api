package com.horadocort.application.service;

import com.horadocort.application.dto.AvailabilityResponse;
import com.horadocort.config.AppProperties;
import com.horadocort.domain.entity.Booking;
import com.horadocort.domain.entity.Tenant;
import com.horadocort.domain.repository.BookingRepository;
import com.horadocort.domain.repository.TenantRepository;
import com.horadocort.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final BookingRepository bookingRepository;
    private final TenantRepository tenantRepository;
    private final AppProperties appProperties;

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(UUID barberId, LocalDate date, int serviceDurationMinutes) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return new AvailabilityResponse(List.of());
        }

        Tenant tenant = tenantRepository.findById(TenantContext.require()).orElseThrow();
        ZoneId zone = ZoneId.of(tenant.getTimezone());

        ZonedDateTime startOfDay = date.atStartOfDay(zone);
        ZonedDateTime endOfDay = date.plusDays(1).atStartOfDay(zone);

        List<Booking> existing = bookingRepository.findActiveByBarberAndDay(barberId, startOfDay, endOfDay);
        ZonedDateTime now = ZonedDateTime.now(zone);

        List<AvailabilityResponse.TimeSlot> slots = new ArrayList<>();
        LocalTime slotTime = LocalTime.of(tenant.getOpeningHour(), 0);
        LocalTime closing = LocalTime.of(tenant.getClosingHour(), 0);

        while (!slotTime.plusMinutes(serviceDurationMinutes).isAfter(closing)) {
            ZonedDateTime slotStart = date.atTime(slotTime).atZone(zone);
            ZonedDateTime slotEnd = slotStart.plusMinutes(serviceDurationMinutes);

            boolean inPast = slotStart.isBefore(now);
            boolean conflict = existing.stream().anyMatch(b ->
                    b.getStartAt().isBefore(slotEnd) && b.getEndAt().isAfter(slotStart)
            );

            slots.add(new AvailabilityResponse.TimeSlot(slotTime, !inPast && !conflict));
            slotTime = slotTime.plusMinutes(appProperties.defaults().slotDurationMinutes());
        }

        return new AvailabilityResponse(slots);
    }
}
