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
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("to", to);
        payload.put("template", templateName);
        payload.put("language", "pt_BR");
        payload.put("variables", variables);

        relayOsWebClient.post()
                .uri("/v1/messages")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(response -> log.info("RelayOS msg enviada — template={}, to={}", templateName, to))
                .doOnError(error -> log.error("Falha envio RelayOS — template={}, to={}, erro={}", templateName, to, error.getMessage()))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }

    public RelayOsProperties.Templates templates() {
        return properties.templates();
    }
}
