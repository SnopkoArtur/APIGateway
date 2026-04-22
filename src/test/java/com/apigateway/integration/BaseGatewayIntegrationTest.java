package com.apigateway.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public abstract class BaseGatewayIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;

    protected static WireMockServer authMock = new WireMockServer(8081);
    protected static WireMockServer userMock = new WireMockServer(8080);

    @BeforeAll
    static void start() {
        authMock.start();
        userMock.start();
    }

    @AfterAll
    static void stop() {
        authMock.stop();
        userMock.stop();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry reg) {
        reg.add("services.auth-url", () -> "http://localhost:8081");
        reg.add("services.user-url", () -> "http://localhost:8080");
        reg.add("jwt.key", () -> "very_long_secret_key_at_least_32_chars_12345");
    }
}