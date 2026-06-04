package com.horadocort.application.service;

import com.horadocort.application.dto.BarberRequest;
import com.horadocort.application.dto.BarberResponse;
import com.horadocort.application.dto.ServiceRequest;
import com.horadocort.application.dto.ServiceResponse;
import com.horadocort.domain.entity.Barber;
import com.horadocort.domain.entity.ServiceCatalog;
import com.horadocort.domain.repository.BarberRepository;
import com.horadocort.domain.repository.ServiceCatalogRepository;
import com.horadocort.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final BarberRepository barberRepository;
    private final ServiceCatalogRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<BarberResponse> listBarbers() {
        return barberRepository.findAllByActiveTrueOrderByName().stream()
                .map(b -> new BarberResponse(b.getId(), b.getName(), b.getAvatarUrl()))
                .toList();
    }

    @Transactional
    public BarberResponse createBarber(BarberRequest request) {
        Barber barber = Barber.builder()
                .tenantId(TenantContext.require())
                .name(request.name())
                .phone(request.phone())
                .avatarUrl(request.avatarUrl())
                .active(true)
                .build();
        Barber saved = barberRepository.save(barber);
        return new BarberResponse(saved.getId(), saved.getName(), saved.getAvatarUrl());
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> listServices() {
        return serviceRepository.findAllByActiveTrueOrderByName().stream()
                .map(s -> new ServiceResponse(s.getId(), s.getName(), s.getDurationMinutes(), s.getPriceCents()))
                .toList();
    }

    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        ServiceCatalog service = ServiceCatalog.builder()
                .tenantId(TenantContext.require())
                .name(request.name())
                .durationMinutes(request.durationMinutes())
                .priceCents(request.priceCents())
                .active(true)
                .build();
        ServiceCatalog saved = serviceRepository.save(service);
        return new ServiceResponse(saved.getId(), saved.getName(), saved.getDurationMinutes(), saved.getPriceCents());
    }
}
