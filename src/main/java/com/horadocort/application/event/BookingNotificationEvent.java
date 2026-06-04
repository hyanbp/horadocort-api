package com.horadocort.application.event;

import com.horadocort.domain.entity.Booking;

public record BookingNotificationEvent(
        Booking booking,
        String tenantName,
        String barberName,
        String barberPhone,
        String serviceName,
        NotificationType type
) {
    public enum NotificationType {
        CUSTOMER_CONFIRMATION,
        CUSTOMER_REMINDER,
        BARBER_NEW_BOOKING
    }
}
