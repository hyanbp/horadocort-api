package com.horadocort.application.service;

import com.horadocort.application.dto.BookingResponse;
import com.horadocort.application.dto.CreateBookingRequest;
import com.horadocort.application.event.BookingNotificationEvent;
import com.horadocort.application.event.BookingNotificationEvent.NotificationType;
import com.horadocort.domain.entity.*;
import com.horadocort.domain.repository.BarberRepository;
import com.horadocort.domain.repository.BookingRepository;
import com.horadocort.domain.repository.ServiceCatalogRepository;
import com.horadocort.domain.repository.TenantRepository;
import com.horadocort.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BarberRepository barberRepository;
    private final ServiceCatalogRepository serviceRepository;
    private final TenantRepository tenantRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public BookingResponse create(CreateBookingRequest request) {
        UUID tenantId = TenantContext.require();
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow();
        ZoneId zone = ZoneId.of(tenant.getTimezone());

        if (request.date().getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("A barbearia não atende aos domingos");
        }

        Barber barber = barberRepository.findById(request.barberId())
                .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado"));
        ServiceCatalog service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

        ZonedDateTime startAt = request.date().atTime(request.time()).atZone(zone);
        ZonedDateTime endAt = startAt.plusMinutes(service.getDurationMinutes());

        if (startAt.isBefore(ZonedDateTime.now(zone))) {
            throw new IllegalArgumentException("Não é possível agendar no passado");
        }

        List<Booking> conflicts = bookingRepository.findActiveByBarberAndDay(
                barber.getId(),
                request.date().atStartOfDay(zone),
                request.date().plusDays(1).atStartOfDay(zone)
        );
        boolean hasConflict = conflicts.stream().anyMatch(b ->
                b.getStartAt().isBefore(endAt) && b.getEndAt().isAfter(startAt)
        );
        if (hasConflict) {
            throw new IllegalStateException("Horário indisponível");
        }

        Booking booking = Booking.builder()
                .tenantId(tenantId)
                .barberId(barber.getId())
                .serviceId(service.getId())
                .customerName(request.customerName())
                .customerPhone(request.customerPhone())
                .startAt(startAt)
                .endAt(endAt)
                .status(BookingStatus.CONFIRMED)
                .reminderSent(false)
                .build();
        Booking saved = bookingRepository.save(booking);

        eventPublisher.publishEvent(new BookingNotificationEvent(
                saved, tenant.getName(), barber.getName(), barber.getPhone(), service.getName(),
                NotificationType.CUSTOMER_CONFIRMATION
        ));
        eventPublisher.publishEvent(new BookingNotificationEvent(
                saved, tenant.getName(), barber.getName(), barber.getPhone(), service.getName(),
                NotificationType.BARBER_NEW_BOOKING
        ));

        return toResponse(saved, barber, service);
    }

    @Transactional
    public void cancel(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listByBarberAndDay(UUID barberId, ZonedDateTime startOfDay, ZonedDateTime endOfDay) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado"));
        return bookingRepository.findActiveByBarberAndDay(barberId, startOfDay, endOfDay).stream()
                .map(b -> {
                    ServiceCatalog service = serviceRepository.findById(b.getServiceId()).orElseThrow();
                    return toResponse(b, barber, service);
                })
                .toList();
    }

    private BookingResponse toResponse(Booking booking, Barber barber, ServiceCatalog service) {
        return new BookingResponse(
                booking.getId(),
                barber.getId(),
                barber.getName(),
                service.getId(),
                service.getName(),
                booking.getCustomerName(),
                booking.getCustomerPhone(),
                booking.getStartAt(),
                booking.getEndAt(),
                booking.getStatus()
        );
    }
}
