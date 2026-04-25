package com.example.car_manager_api.api.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.car_manager_api.service.ExchangeRateService;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityMvcIT {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ExchangeRateService exchangeRateService;

    @BeforeEach
    public void stubCotacao() {
        given(exchangeRateService.getUsdToBrl()).willReturn(BigDecimal.ONE);
    }

    @Test
    @DisplayName("GET /veiculos sem token retorna 401")
    public void semAuth401Test() throws Exception {
        mvc.perform(get("/veiculos").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /veiculos como USER retorna 403")
    public void userSemPermissao403Test() throws Exception {
        mvc.perform(post("/veiculos")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"AAA1111\",\"marca\":\"X\",\"ano\":2020,\"cor\":\"Y\",\"precoUsd\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /veiculos como USER retorna 200")
    public void userPodeConsultarTest() throws Exception {
        mvc.perform(get("/veiculos").with(user("user").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /veiculos/relatorios/por-marca/pdf sem token retorna 401")
    public void pdfSemToken401Test() throws Exception {
        mvc.perform(get("/veiculos/relatorios/por-marca/pdf").accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /veiculos/relatorios/por-marca/pdf como USER retorna application/pdf")
    public void pdfComUser200Test() throws Exception {
        mvc.perform(get("/veiculos/relatorios/por-marca/pdf")
                        .with(user("user").roles("USER"))
                        .accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(result -> {
                    byte[] body = result.getResponse().getContentAsByteArray();
                    Assertions.assertTrue(body.length > 200, "PDF gerado pelo Jasper deve ter tamanho minimo");
                    Assertions.assertEquals('%', body[0]);
                    Assertions.assertEquals('P', body[1]);
                    Assertions.assertEquals('D', body[2]);
                    Assertions.assertEquals('F', body[3]);
                });
    }
}
