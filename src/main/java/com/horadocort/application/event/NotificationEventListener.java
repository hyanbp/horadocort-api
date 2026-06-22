package com.horadocort.application.event;

import com.horadocort.config.RelayOsProperties;
import com.horadocort.infrastructure.client.RelayOsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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

        log.info("Disparando template {} para tenant {} (to={})", templateName, event.tenantName(), to);

        try {
            relayOsClient.sendMessage(to, templateName, variables);
        } catch (IllegalArgumentException e) {
            log.warn("Notificação ignorada — template={}, to={}, motivo={}", templateName, to, e.getMessage());
        } catch (Exception e) {
            log.error("Falha inesperada ao notificar — template={}, to={}", templateName, to, e);
        }
    }

    private Map<String, String> buildVariables(BookingNotificationEvent event) {
        Map<String, String> vars = new LinkedHashMap<>();
        String date = event.booking().getStartAt().format(DATE_FMT);
        String time = event.booking().getStartAt().format(TIME_FMT);
        String customer = capitalize(event.booking().getCustomerName());
        String tenant = capitalize(event.tenantName());
        String barber = capitalize(event.barberName());
        String service = capitalize(event.serviceName());

        switch (event.type()) {
            case CUSTOMER_CONFIRMATION -> {
                vars.put("1", customer);
                vars.put("2", tenant);
                vars.put("3", date);
                vars.put("4", time);
                vars.put("5", barber);
                vars.put("6", service);
            }
            case CUSTOMER_REMINDER -> {
                vars.put("1", customer);
                vars.put("2", tenant);
                vars.put("3", time);
            }
            case BARBER_NEW_BOOKING -> {
                vars.put("1", barber);
                vars.put("2", customer);
                vars.put("3", service);
                vars.put("4", date);
                vars.put("5", time);
            }
        }
        return vars;
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return Arrays.stream(s.trim().split("\\s+"))
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}