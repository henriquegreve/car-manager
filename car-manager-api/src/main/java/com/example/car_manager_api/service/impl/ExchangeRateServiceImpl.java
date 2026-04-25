package com.example.car_manager_api.service.impl;

import com.example.car_manager_api.service.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@Slf4j
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private static final String CACHE_KEY = "fx:usd-brl";

    @Value("${app.exchange-rate.awesome-url}")
    private String awesomeUrl;

    @Value("${app.exchange-rate.frankfurter-url}")
    private String frankfurterUrl;

    @Value("${app.exchange-rate.cache-ttl-seconds:120}")
    private long cacheTtlSeconds;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redis;

    public ExchangeRateServiceImpl(WebClient webClient,
                                   ObjectMapper objectMapper,
                                   @Autowired(required = false) StringRedisTemplate redis) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.redis = redis;
    }

    @Override
    public BigDecimal getUsdToBrl() {
        BigDecimal cache = lerCache();
        if(cache != null){
            return cache;
        }

        try {
            BigDecimal cotacao = buscarAwesome();
            gravarCache(cotacao);
            return cotacao;
        } catch (Exception e) {
            log.warn(" Awesome API falhou, tentando Frankfurter: {} ", e.getMessage());
            BigDecimal cotacao = buscarFrankfurter();
            gravarCache(cotacao);
            return cotacao;
        }
    }

    private BigDecimal lerCache() {
        if(redis == null) return null;
        String valor = redis.opsForValue().get(CACHE_KEY);
        if(valor == null || valor.isBlank()) return null;
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            redis.delete(CACHE_KEY);
            return null;
        }
    }

    private void gravarCache(BigDecimal cotacao) {
        if(redis == null) return;
        redis.opsForValue().set(CACHE_KEY, cotacao.toPlainString(), Duration.ofSeconds(cacheTtlSeconds));
    }

    private BigDecimal buscarAwesome() {
        String corpo = webClient.get().uri(awesomeUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(5));
        try {
            JsonNode root = objectMapper.readTree(corpo);
            JsonNode usdbrl = root.get("USDBRL");
            return new BigDecimal(usdbrl.get("bid").asText().replace(",", "."));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao parsear retorno da Awesome API", e);
        }
    }

    private BigDecimal buscarFrankfurter() {
        String corpo = webClient.get().uri(frankfurterUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(5));
        try {
            JsonNode root = objectMapper.readTree(corpo);
            return new BigDecimal(root.get("rates").get("BRL").asText());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao parsear retorno do Frankfurter", e);
        }
    }

}
