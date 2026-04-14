package com.apigateway.service;

import com.apigateway.dto.RegistrationDto;
import reactor.core.publisher.Mono;


/**
 * Service for registration flow
 */
public interface RegistrationService {
    /**
     * Provides flow for registration with saving both data in auth and user services
     *
     * @param dto data for registration
     * @return result of creation
     */
    Mono<Void> register(RegistrationDto dto);
}
