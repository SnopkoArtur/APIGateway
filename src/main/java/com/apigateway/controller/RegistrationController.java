package com.apigateway.controller;


import com.apigateway.dto.RegistrationDto;
import com.apigateway.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    @PostMapping("/api/v1/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> register(@RequestBody @Valid RegistrationDto dto) {
        return registrationService.register(dto);
    }
}