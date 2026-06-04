package com.horadocort.application.service;

import com.horadocort.application.dto.LoginRequest;
import com.horadocort.application.dto.TokenResponse;
import com.horadocort.domain.entity.Tenant;
import com.horadocort.domain.entity.User;
import com.horadocort.domain.repository.TenantRepository;
import com.horadocort.domain.repository.UserRepository;
import com.horadocort.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public TokenResponse login(LoginRequest request) {
        Tenant tenant = tenantRepository.findBySlug(request.tenantSlug())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        User user = userRepository.findByTenantIdAndEmail(tenant.getId(), request.email())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        String token = jwtService.issueToken(user.getId(), tenant.getId(), user.getEmail(), user.getRole().name());
        return new TokenResponse(
                token,
                tenant.getId(),
                tenant.getSlug(),
                tenant.getName(),
                user.getName(),
                user.getRole().name()
        );
    }
}
