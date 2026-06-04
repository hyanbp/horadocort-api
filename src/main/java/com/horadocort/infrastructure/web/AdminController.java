package com.horadocort.infrastructure.web;

import com.horadocort.application.dto.*;
import com.horadocort.application.service.BookingService;
import com.horadocort.application.service.CatalogService;
import com.horadocort.domain.entity.Tenant;
import com.horadocort.domain.repository.TenantRepository;
import com.horadocort.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CatalogService catalogService;
    private final BookingService bookingService;
    private final TenantRepository tenantRepository;

    @GetMapping("/barbers")
    public List<BarberResponse> listBarbers() {
        return catalogService.listBarbers();
    }

    @PostMapping("/barbers")
    @ResponseStatus(HttpStatus.CREATED)
    public BarberResponse createBarber(@Valid @RequestBody BarberRequest request) {
        return catalogService.createBarber(request);
    }

    @GetMapping("/services")
    public List<ServiceResponse> listServices() {
        return catalogService.listServices();
    }

    @PostMapping("/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse createService(@Valid @RequestBody ServiceRequest request) {
        return catalogService.createService(request);
    }

    @GetMapping("/bookings")
    @Transactional(readOnly = true)
    public List<BookingResponse> bookings(
            @RequestParam UUID barberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Tenant tenant = tenantRepository.findById(TenantContext.require()).orElseThrow();
        ZoneId zone = ZoneId.of(tenant.getTimezone());
        ZonedDateTime startOfDay = date.atStartOfDay(zone);
        ZonedDateTime endOfDay = date.plusDays(1).atStartOfDay(zone);
        return bookingService.listByBarberAndDay(barberId, startOfDay, endOfDay);
    }

    @DeleteMapping("/bookings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID id) {
        bookingService.cancel(id);
    }
}
