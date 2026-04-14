package com.apigateway.controller;


import com.apigateway.dto.RegistrationDto;
import com.apigateway.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST-controller for user registration
 * Provides single endpoint for registration
 */
@RestController
@RequestMapping("/api/v1/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    /**
     * Automatizes cycle of registration by putting data both into auth and user services
     * Rollbacks user creation in case of failure
     * @param dto user data needed for registration
     * @return 201 on success
     *
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> register(@RequestBody RegistrationDto dto) {
        return registrationService.register(dto);
    }
}