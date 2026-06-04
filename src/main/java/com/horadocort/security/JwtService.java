package com.horadocort.security;

import com.horadocort.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtService {

    private static final String ALG = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public String issueToken(UUID userId, UUID tenantId, String email, String role) {
        long expiresAt = Instant.now().plusSeconds(appProperties.jwt().expirationHours() * 3600L).getEpochSecond();

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = Map.of(
                "sub", userId.toString(),
                "tenantId", tenantId.toString(),
                "email", email,
                "role", role,
                "exp", expiresAt
        );

        String headerB64 = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(header));
        String payloadB64 = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
        String signature = sign(headerB64 + "." + payloadB64);

        return headerB64 + "." + payloadB64 + "." + signature;
    }

    @SneakyThrows
    public JwtClaims parseAndValidate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new SecurityException("Token inválido");
        }
        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!expectedSignature.equals(parts[2])) {
            throw new SecurityException("Assinatura inválida");
        }
        Map<String, Object> payload = objectMapper.readValue(URL_DECODER.decode(parts[1]), Map.class);
        long exp = ((Number) payload.get("exp")).longValue();
        if (Instant.now().getEpochSecond() > exp) {
            throw new SecurityException("Token expirado");
        }
        return new JwtClaims(
                UUID.fromString((String) payload.get("sub")),
                UUID.fromString((String) payload.get("tenantId")),
                (String) payload.get("email"),
                (String) payload.get("role")
        );
    }

    @SneakyThrows
    private String sign(String data) {
        Mac mac = Mac.getInstance(ALG);
        mac.init(new SecretKeySpec(appProperties.jwt().secret().getBytes(StandardCharsets.UTF_8), ALG));
        return URL_ENCODER.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    public record JwtClaims(UUID userId, UUID tenantId, String email, String role) {
    }
}
