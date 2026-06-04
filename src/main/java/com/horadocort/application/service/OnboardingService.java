package com.horadocort.application.service;

import com.horadocort.application.dto.OnboardingRequest;
import com.horadocort.application.dto.OnboardingResponse;
import com.horadocort.config.AppProperties;
import com.horadocort.domain.entity.*;
import com.horadocort.domain.repository.ServiceCatalogRepository;
import com.horadocort.domain.repository.TenantRepository;
import com.horadocort.domain.repository.UserRepository;
import com.horadocort.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ServiceCatalogRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    @Transactional
    public OnboardingResponse signup(OnboardingRequest request) {
        if (tenantRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("Esse identificador já está em uso");
        }
        if (tenantRepository.existsByOwnerEmail(request.ownerEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        Tenant tenant = Tenant.builder()
                .slug(request.slug())
                .name(request.barbershopName())
                .ownerEmail(request.ownerEmail())
                .ownerPhone(request.ownerPhone())
                .plan(TenantPlan.TRIAL)
                .status(TenantStatus.ACTIVE)
                .openingHour(appProperties.defaults().openingHour())
                .closingHour(appProperties.defaults().closingHour())
                .timezone("America/Sao_Paulo")
                .trialEndsAt(ZonedDateTime.now().plusDays(14))
                .build();
        tenantRepository.save(tenant);

        User owner = User.builder()
                .tenantId(tenant.getId())
                .email(request.ownerEmail())
                .passwordHash(passwordEncoder.encode(request.ownerPassword()))
                .name(request.ownerName())
                .role(UserRole.OWNER)
                .active(true)
                .build();
        userRepository.save(owner);

        seedDefaultServices(tenant.getId());

        String token = jwtService.issueToken(owner.getId(), tenant.getId(), owner.getEmail(), owner.getRole().name());
        return new OnboardingResponse(tenant.getId(), tenant.getSlug(), "/" + tenant.getSlug(), token);
    }

    private void seedDefaultServices(java.util.UUID tenantId) {
        List<ServiceCatalog> defaults = List.of(
                ServiceCatalog.builder().tenantId(tenantId).name("Corte masculino").durationMinutes(30).priceCents(5000).active(true).build(),
                ServiceCatalog.builder().tenantId(tenantId).name("Barba").durationMinutes(30).priceCents(3500).active(true).build(),
                ServiceCatalog.builder().tenantId(tenantId).name("Corte + barba").durationMinutes(60).priceCents(7500).active(true).build()
        );
        serviceRepository.saveAll(defaults);
    }
}
