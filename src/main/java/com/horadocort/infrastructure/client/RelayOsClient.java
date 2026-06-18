package com.horadocort.infrastructure.client;

import com.horadocort.config.RelayOsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RelayOsClient {

    private final WebClient relayOsWebClient;
    private final RelayOsProperties properties;

    public void sendMessage(String to, String templateName, Map<String, String> variables) {
        String normalizedTo = normalizeToE164(to);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("to", normalizedTo);
        payload.put("template", templateName);
        payload.put("language", "pt_BR");
        payload.put("variables", variables);

        relayOsWebClient.post()
                .uri("/v1/messages")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(response -> log.info("RelayOS msg enviada — template={}, to={}", templateName, normalizedTo))
                .doOnError(error -> log.error("Falha envio RelayOS — template={}, to={}, erro={} ({})",
                        templateName, normalizedTo, error.getMessage(), error.getClass().getSimpleName()))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }

    public RelayOsProperties.Templates templates() {
        return properties.templates();
    }

    private String normalizeToE164(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Telefone vazio");
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.startsWith("55") && (digits.length() == 12 || digits.length() == 13)) {
            return "+" + digits;
        }
        if (digits.length() == 10 || digits.length() == 11) {
            return "+55" + digits;
        }
        throw new IllegalArgumentException("Telefone inválido: " + phone);
    }
}