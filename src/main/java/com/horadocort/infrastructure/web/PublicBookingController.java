package com.horadocort.infrastructure.web;

import com.horadocort.application.dto.*;
import com.horadocort.application.service.AvailabilityService;
import com.horadocort.application.service.BookingService;
import com.horadocort.application.service.CatalogService;
import com.horadocort.domain.entity.ServiceCatalog;
import com.horadocort.domain.entity.Tenant;
import com.horadocort.domain.repository.ServiceCatalogRepository;
import com.horadocort.domain.repository.TenantRepository;
import com.horadocort.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/t/{slug}/public")
@RequiredArgsConstructor
public class PublicBookingController {

    private final CatalogService catalogService;
    private final AvailabilityService availabilityService;
    private final BookingService bookingService;
    private final ServiceCatalogRepository serviceRepository;
    private final TenantRepository tenantRepository;

    @GetMapping("/info")
    @Transactional(readOnly = true)
    public PublicTenantResponse info(@PathVariable String slug) {
        Tenant tenant = tenantRepository.findById(TenantContext.require()).orElseThrow();
        return new PublicTenantResponse(
                tenant.getSlug(), tenant.getName(),
                tenant.getOpeningHour(), tenant.getClosingHour(), tenant.getTimezone()
        );
    }

    @GetMapping("/barbers")
    public List<BarberResponse> barbers(@PathVariable String slug) {
        return catalogService.listBarbers();
    }

    @GetMapping("/services")
    public List<ServiceResponse> services(@PathVariable String slug) {
        return catalogService.listServices();
    }

    @GetMapping("/availability")
    @Transactional(readOnly = true)
    public AvailabilityResponse availability(
            @PathVariable String slug,
            @RequestParam UUID barberId,
            @RequestParam UUID serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        ServiceCatalog service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));
        return availabilityService.getAvailability(barberId, date, service.getDurationMinutes());
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@PathVariable String slug, @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(request);
    }
}
