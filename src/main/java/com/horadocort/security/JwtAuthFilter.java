package com.horadocort.security;

import com.horadocort.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                JwtService.JwtClaims claims = jwtService.parseAndValidate(auth.substring(7));
                var authentication = new UsernamePasswordAuthenticationToken(
                        claims.userId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + claims.role()))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // JWT é a fonte de verdade do tenant em rotas autenticadas
                TenantContext.set(claims.tenantId());
            } catch (SecurityException ignored) {
                // token inválido — segue como anônimo
            }
        }
        chain.doFilter(request, response);
    }
}
