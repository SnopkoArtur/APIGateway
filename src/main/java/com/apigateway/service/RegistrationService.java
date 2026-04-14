package com.apigateway.service;

import com.apigateway.dto.RegistrationDto;
import com.apigateway.dto.UserResponseDto;
import com.apigateway.utils.InternalJwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {
    private final WebClient.Builder webClientBuilder;
    private final InternalJwtProvider internalJwtProvider;
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    @Value("${services.auth-url}") String authUrl;
    @Value("${services.user-url}") String userUrl;

    public Mono<Void> register(RegistrationDto dto) {

        String internalAdminToken = internalJwtProvider.createInternalToken("GATEWAY_ORCHESTRATOR", "ADMIN", 0L);

        return saveToUser(dto, internalAdminToken)
                .flatMap(userResponse -> {
                    Long generatedId = userResponse.getId();
                    log.info("User created with ID: {}, now saving to Auth", generatedId);

                    return saveToAuth(dto, generatedId, internalAdminToken)
                            .onErrorResume(e -> {
                                log.error("Auth Service failed, rolling back User Service for ID: {}", generatedId);
                                return rollbackUser(generatedId, internalAdminToken)
                                        .then(Mono.error(e));
                            });
                });
    }

    private Mono<UserResponseDto> saveToUser(RegistrationDto dto, String token) {
        return webClientBuilder.build().post()
                .uri(userUrl + "/api/v1/users")
                .header(AUTHORIZATION, BEARER + token)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(UserResponseDto.class);
    }

    private Mono<Void> saveToAuth(RegistrationDto dto, Long userId, String token) {
        java.util.Map<String, Object> authData = new java.util.HashMap<>();
        authData.put("login", dto.getLogin());
        authData.put("password", dto.getPassword());
        authData.put("role", dto.getRole());
        authData.put("userId", userId);

        return webClientBuilder.build().post()
                .uri(authUrl + "/api/v1/auth/save")
                .header(AUTHORIZATION, BEARER + token)
                .bodyValue(authData)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    private Mono<Void> rollbackUser(Long userId, String token) {
        return webClientBuilder.build().delete()
                .uri(userUrl + "/api/v1/users/" + userId)
                .header(AUTHORIZATION, BEARER + token)
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}
