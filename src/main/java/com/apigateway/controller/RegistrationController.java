package com.apigateway.controller;


import com.apigateway.dto.RegistrationDto;
import com.apigateway.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> register(@RequestBody RegistrationDto dto) {
        return registrationService.register(dto);
    }
}