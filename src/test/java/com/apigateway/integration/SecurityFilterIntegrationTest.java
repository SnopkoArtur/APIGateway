package com.apigateway.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

class SecurityFilterIntegrationTest extends BaseGatewayIntegrationTest {

    @Test
    void shouldReturn401_WhenNoTokenProvided() {
        webTestClient.get().uri("/api/v1/users/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowAccess_ToLoginAndRegisterWithoutToken() {
        authMock.stubFor(WireMock.post(urlEqualTo("/api/v1/auth/login"))
                .willReturn(aResponse().withStatus(200)));

        webTestClient.post().uri("/api/v1/auth/login")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldReturn401_WhenTokenIsExpired() {
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("test")
                .setExpiration(new java.util.Date(System.currentTimeMillis() - 10000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("very_long_secret_key_at_least_32_chars_12345".getBytes()))
                .compact();

        webTestClient.get().uri("/api/v1/users/1")
                .header("Authorization", "Bearer " + expiredToken)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid JWT token");
    }

    @Test
    void shouldReturn401_WhenTokenSignatureIsInvalid() {
        String wrongSignedToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("test")
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("WRONG_SECRET_KEY_THAT_IS_ALSO_LONG_ENOUGH_123".getBytes()))
                .compact();

        webTestClient.get().uri("/api/v1/users/1")
                .header("Authorization", "Bearer " + wrongSignedToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}