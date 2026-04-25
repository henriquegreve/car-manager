package com.example.car_manager_api.api.integration;

import com.example.car_manager_api.service.ExchangeRateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class VeiculoApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("Fluxo: login admin -> criar veiculo -> listar -> detalhar")
    public void fluxoCompletoTest() throws Exception {
        given(exchangeRateService.getUsdToBrl()).willReturn(new BigDecimal("5.00"));
        String placa = "F" + String.format("%06d", (int) (System.nanoTime() % 1_000_000));

        MvcResult login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("accessToken").exists())
                .andReturn();

        String token = JsonMapper.builder().build()
                .readTree(login.getResponse().getContentAsString())
                .path("accessToken")
                .asText();

        String veiculoJson = String.format(
                "{\"placa\":\"%s\",\"marca\":\"Fiat\",\"ano\":2023,\"cor\":\"Prata\",\"precoUsd\":25000}",
                placa);

        MvcResult criado = mvc.perform(post("/veiculos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(veiculoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("placa").value(placa))
                .andReturn();

        long id = JsonMapper.builder().build()
                .readTree(criado.getResponse().getContentAsString())
                .path("id")
                .asLong();

        mvc.perform(get("/veiculos?marca=Fiat")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content[0].placa").value(placa));

        mvc.perform(get("/veiculos/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("marca").value("Fiat"));
    }

    @Test
    @DisplayName("Login com credenciais invalidas retorna 401")
    public void loginInvalidoTest() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Segunda criacao com mesma placa retorna 409")
    public void placaDuplicada409Test() throws Exception {
        given(exchangeRateService.getUsdToBrl()).willReturn(new BigDecimal("5.00"));
        String placa = "D" + String.format("%06d", (int) (System.nanoTime() % 1_000_000));

        MvcResult login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = JsonMapper.builder().build().readTree(login.getResponse().getContentAsString());
        String token = root.path("accessToken").asText();

        String body = String.format(
                "{\"placa\":\"%s\",\"marca\":\"VW\",\"ano\":2021,\"cor\":\"Azul\",\"precoUsd\":10000}",
                placa);
        mvc.perform(post("/veiculos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mvc.perform(post("/veiculos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("errors[0]").value("Placa já cadastrada."));
    }
}
