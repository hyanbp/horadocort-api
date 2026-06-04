package com.horadocort.tenant;

import com.horadocort.domain.entity.Tenant;
import com.horadocort.domain.repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private static final Pattern PATH_PATTERN = Pattern.compile("^/api/v1/t/([a-z0-9-]+)(/.*)?$");

    private final TenantRepository tenantRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            resolveTenant(request);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void resolveTenant(HttpServletRequest request) {
        String slug = extractSlug(request.getRequestURI());
        if (slug == null) {
            return;
        }
        Tenant tenant = tenantRepository.findBySlug(slug).orElse(null);
        if (tenant != null && tenant.isActive()) {
            TenantContext.set(tenant.getId());
        }
    }

    private String extractSlug(String uri) {
        Matcher matcher = PATH_PATTERN.matcher(uri);
        return matcher.matches() ? matcher.group(1) : null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/v1/public/")
                || uri.startsWith("/api/v1/auth/")
                || uri.startsWith("/api/v1/onboarding/")
                || uri.startsWith("/actuator/");
    }
}
