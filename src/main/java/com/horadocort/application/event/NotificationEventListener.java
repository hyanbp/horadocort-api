package com.horadocort.application.event;

import com.horadocort.config.RelayOsProperties;
import com.horadocort.infrastructure.client.RelayOsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final RelayOsClient relayOsClient;

    @Async
    @EventListener
    public void handle(BookingNotificationEvent event) {
        RelayOsProperties.Templates templates = relayOsClient.templates();
        String templateName = switch (event.type()) {
            case CUSTOMER_CONFIRMATION -> templates.bookingConfirmed();
            case CUSTOMER_REMINDER -> templates.bookingReminder();
            case BARBER_NEW_BOOKING -> templates.barberNotification();
        };

        String to = switch (event.type()) {
            case CUSTOMER_CONFIRMATION, CUSTOMER_REMINDER -> event.booking().getCustomerPhone();
            case BARBER_NEW_BOOKING -> event.barberPhone();
        };

        Map<String, String> variables = buildVariables(event);

        log.info("Disparando template {} para tenant {}", templateName, event.tenantName());
        relayOsClient.sendMessage(to, templateName, variables);
    }

    private Map<String, String> buildVariables(BookingNotificationEvent event) {
        Map<String, String> vars = new LinkedHashMap<>();
        String date = event.booking().getStartAt().format(DATE_FMT);
        String time = event.booking().getStartAt().format(TIME_FMT);

        switch (event.type()) {
            case CUSTOMER_CONFIRMATION -> {
                vars.put("1", event.booking().getCustomerName());
                vars.put("2", event.tenantName());
                vars.put("3", date);
                vars.put("4", time);
                vars.put("5", event.barberName());
                vars.put("6", event.serviceName());
            }
            case CUSTOMER_REMINDER -> {
                // booking_reminder: {{1}} cliente, {{2}} barbearia, {{3}} hora
                vars.put("1", event.booking().getCustomerName());
                vars.put("2", event.tenantName());
                vars.put("3", time);
            }
            case BARBER_NEW_BOOKING -> {
                // barber_new_booking: {{1}} barbeiro, {{2}} cliente, {{3}} serviço, {{4}} data, {{5}} hora
                vars.put("1", event.barberName());
                vars.put("2", event.booking().getCustomerName());
                vars.put("3", event.serviceName());
                vars.put("4", date);
                vars.put("5", time);
            }
        }
        return vars;
    }
}
