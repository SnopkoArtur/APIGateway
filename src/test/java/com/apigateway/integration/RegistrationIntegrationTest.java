package com.apigateway.integration;


import com.apigateway.dto.RegistrationDto;
import com.apigateway.dto.Role;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class RegistrationIntegrationTest extends BaseGatewayIntegrationTest {

    @BeforeEach
    void clearMocks() {
        authMock.resetAll();
        userMock.resetAll();

        com.github.tomakehurst.wiremock.client.WireMock.configureFor("localhost", authMock.port());
        com.github.tomakehurst.wiremock.client.WireMock.configureFor("localhost", userMock.port());
    }

    @Test
    void registration_Success_ShouldCallBothServices(){
        RegistrationDto dto = new RegistrationDto("login", "pass", Role.USER, "Name", "Surname", "email@test.com", null);

        userMock.stubFor(WireMock.post(urlEqualTo("/api/v1/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 123}")));

        authMock.stubFor(WireMock.post(urlEqualTo("/api/v1/auth/save"))
                .willReturn(aResponse().withStatus(200)));

        webTestClient.post().uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated();

        userMock.verify(postRequestedFor(urlEqualTo("/api/v1/users")));
        authMock.verify(postRequestedFor(urlEqualTo("/api/v1/auth/save")));
    }

    @Test
    void shouldReturn503_WhenDownstreamServiceIsDown() {
        userMock.resetAll();
        userMock.stubFor(WireMock.post(urlEqualTo("/api/v1/users"))
                .willReturn(aResponse().withStatus(503)));

        RegistrationDto dto = new RegistrationDto("test_service_down", "password", Role.USER, "name", "surname", "unavailable@test.com", null);


        webTestClient.post().uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Service is temporarily unavailable. Please try again later.");
    }
}