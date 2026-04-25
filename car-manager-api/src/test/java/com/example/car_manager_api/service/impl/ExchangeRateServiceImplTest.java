package com.example.car_manager_api.service.impl;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExchangeRateServiceImplTest {

    private MockWebServer server;
    private WebClient webClient;

    @BeforeEach
    public void startServer() throws Exception {
        server = new MockWebServer();
        server.start();
        webClient = WebClient.builder().build();
    }

    @AfterEach
    public void shutdown() throws Exception {
        server.shutdown();
    }

    @Test
    @DisplayName("Deve obter cotacao pela Awesome API")
    public void awesomeSuccessTest() {
        server.enqueue(new MockResponse()
                .setBody("{\"USDBRL\":{\"bid\":\"5,25\"}}")
                .addHeader("Content-Type", "application/json"));

        ExchangeRateServiceImpl svc = newService(null);
        assertThat(svc.getUsdToBrl()).isEqualByComparingTo("5.25");
    }

    @Test
    @DisplayName("Deve usar Frankfurter quando Awesome falhar")
    public void frankfurterFallbackTest() {
        server.enqueue(new MockResponse().setResponseCode(500));
        server.enqueue(new MockResponse()
                .setBody("{\"rates\":{\"BRL\":\"5.40\"}}")
                .addHeader("Content-Type", "application/json"));

        ExchangeRateServiceImpl svc = newService(null);
        assertThat(svc.getUsdToBrl()).isEqualByComparingTo("5.40");
    }

    @Test
    @DisplayName("Deve lancar quando ambas APIs falharem no parse")
    public void bothFailTest() {
        server.enqueue(new MockResponse().setBody("invalid"));
        server.enqueue(new MockResponse().setBody("invalid"));

        ExchangeRateServiceImpl svc = newService(null);
        assertThatThrownBy(svc::getUsdToBrl).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deve retornar cotacao do cache Redis sem chamar rede")
    public void cacheHitTest() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get("fx:usd-brl")).thenReturn("6.10");

        ExchangeRateServiceImpl svc = newService(redis);
        assertThat(svc.getUsdToBrl()).isEqualByComparingTo("6.10");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    @DisplayName("Deve ignorar cache invalido e buscar Awesome")
    public void corruptCacheClearsAndFetchesTest() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get("fx:usd-brl")).thenReturn("not-a-number");

        server.enqueue(new MockResponse()
                .setBody("{\"USDBRL\":{\"bid\":\"4.90\"}}")
                .addHeader("Content-Type", "application/json"));

        ExchangeRateServiceImpl svc = newService(redis);
        assertThat(svc.getUsdToBrl()).isEqualByComparingTo("4.90");
        verify(redis).delete("fx:usd-brl");
        verify(ops).set(eq("fx:usd-brl"), eq("4.90"), any(Duration.class));
    }

    private ExchangeRateServiceImpl newService(StringRedisTemplate redis) {
        ExchangeRateServiceImpl svc = new ExchangeRateServiceImpl(webClient, JsonMapper.builder().build(), redis);
        ReflectionTestUtils.setField(svc, "awesomeUrl", server.url("/awesome").toString());
        ReflectionTestUtils.setField(svc, "frankfurterUrl", server.url("/frank").toString());
        ReflectionTestUtils.setField(svc, "cacheTtlSeconds", 120L);
        return svc;
    }
}
