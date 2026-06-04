package com.horadocort.infrastructure.scheduler;

import com.horadocort.application.event.BookingNotificationEvent;
import com.horadocort.application.event.BookingNotificationEvent.NotificationType;
import com.horadocort.config.AppProperties;
import com.horadocort.domain.entity.Barber;
import com.horadocort.domain.entity.Booking;
import com.horadocort.domain.entity.BookingStatus;
import com.horadocort.domain.entity.ServiceCatalog;
import com.horadocort.domain.entity.Tenant;
import com.horadocort.domain.repository.BarberRepository;
import com.horadocort.domain.repository.BookingRepository;
import com.horadocort.domain.repository.ServiceCatalogRepository;
import com.horadocort.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final BookingRepository bookingRepository;
    private final BarberRepository barberRepository;
    private final ServiceCatalogRepository serviceRepository;
    private final TenantRepository tenantRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AppProperties appProperties;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    @Transactional
    public void dispatchReminders() {
        // Janela calculada em UTC; cada booking guarda timezone via ZonedDateTime
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime windowEnd = now.plusMinutes(appProperties.defaults().reminderWindowMinutes());

        // Native query: ignora o filtro de tenant de propósito (scheduler é cross-tenant)
        List<Booking> bookings = bookingRepository.findPendingRemindersAcrossTenants(
                BookingStatus.CONFIRMED.name(), now, windowEnd
        );
        if (bookings.isEmpty()) {
            return;
        }

        log.info("Disparando lembretes para {} agendamentos (cross-tenant)", bookings.size());
        for (Booking booking : bookings) {
            Tenant tenant = tenantRepository.findById(booking.getTenantId()).orElse(null);
            Barber barber = barberRepository.findByIdUnfiltered(booking.getBarberId()).orElse(null);
            ServiceCatalog service = serviceRepository.findByIdUnfiltered(booking.getServiceId()).orElse(null);
            if (tenant == null || barber == null || service == null) {
                continue;
            }

            eventPublisher.publishEvent(new BookingNotificationEvent(
                    booking, tenant.getName(), barber.getName(), barber.getPhone(), service.getName(),
                    NotificationType.CUSTOMER_REMINDER
            ));

            booking.setReminderSent(true);
            bookingRepository.save(booking);
        }
    }
}
