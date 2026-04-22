package com.apigateway.service;

import com.apigateway.dto.RegistrationDto;
import com.apigateway.utils.InternalJwtProvider;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    private static MockWebServer mockBackEnd;
    private RegistrationService registrationService;

    @Mock
    private InternalJwtProvider internalJwtProvider;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());

        registrationService = new RegistrationServiceImpl(WebClient.builder(), internalJwtProvider);

        ReflectionTestUtils.setField(registrationService, "authUrl", baseUrl);
        ReflectionTestUtils.setField(registrationService, "userUrl", baseUrl);

        when(internalJwtProvider.createInternalToken(anyString(), anyString(), anyLong()))
                .thenReturn("test-token");
    }

    @Test
    void register_Success() throws InterruptedException {
        mockBackEnd.enqueue(new MockResponse()
                .setBody("{\"id\": 10}")
                .addHeader("Content-Type", "application/json")
                .setResponseCode(201));

        mockBackEnd.enqueue(new MockResponse().setResponseCode(200));

        RegistrationDto dto = new RegistrationDto();
        dto.setLogin("test");

        StepVerifier.create(registrationService.register(dto))
                .verifyComplete();

        var requestToUser = mockBackEnd.takeRequest();
        var requestToAuth = mockBackEnd.takeRequest();

        assertEquals("POST", requestToUser.getMethod());
        assertEquals("POST", requestToAuth.getMethod());
    }

    @Test
    void register_AuthFails_ShouldRollback() throws InterruptedException {
        mockBackEnd.enqueue(new MockResponse()
                .setBody("{\"id\": 10}")
                .addHeader("Content-Type", "application/json")
                .setResponseCode(201));

        mockBackEnd.enqueue(new MockResponse().setResponseCode(500));

        mockBackEnd.enqueue(new MockResponse().setResponseCode(200));

        RegistrationDto dto = new RegistrationDto();
        dto.setLogin("test");

        StepVerifier.create(registrationService.register(dto))
                .expectError()
                .verify();

        assertEquals(3, mockBackEnd.getRequestCount());

        RecordedRequest lastRequest = null;
        for(int i=0; i<3; i++) lastRequest = mockBackEnd.takeRequest();
        assertEquals("DELETE", lastRequest.getMethod());
    }
}