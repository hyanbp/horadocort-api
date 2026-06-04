package com.horadocort.infrastructure.web;

import com.horadocort.application.dto.OnboardingRequest;
import com.horadocort.application.dto.OnboardingResponse;
import com.horadocort.application.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OnboardingResponse signup(@Valid @RequestBody OnboardingRequest request) {
        return onboardingService.signup(request);
    }
}
