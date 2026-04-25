package com.example.car_manager_api.api.resource;

import com.example.car_manager_api.api.dto.VeiculoDTO;
import com.example.car_manager_api.model.entity.Veiculo;
import com.example.car_manager_api.model.repository.VeiculoRepository;
import com.example.car_manager_api.security.JwtTokenService;
import com.example.car_manager_api.service.ExchangeRateService;
import com.example.car_manager_api.service.RelatorioService;
import com.example.car_manager_api.service.VeiculoService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = VeiculoController.class)
@AutoConfigureMockMvc
public class VeiculoControllerTest {

    static String VEICULO_API = "/veiculos";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    VeiculoService service;

    @MockitoBean
    ExchangeRateService exchangeRateService;

    @MockitoBean
    RelatorioService relatorioService;

    @MockitoBean
    JwtTokenService jwtTokenService;

    @Test
    @DisplayName("Deve criar um veiculo com sucesso.")
    @WithMockUser(roles = "ADMIN")
    public void createVeiculoTest() throws Exception {

        VeiculoDTO dto = createNewVeiculo();
        Veiculo saved = Veiculo.builder().id(10L)
                .placa("ABC1234").marca("Toyota").ano(2020).cor("Preto")
                .precoUsd(new BigDecimal("20000.00")).ativo(true).build();

        BDDMockito.given(service.save(Mockito.any(Veiculo.class))).willReturn(saved);
        BDDMockito.given(exchangeRateService.getUsdToBrl()).willReturn(new BigDecimal("5.00"));

        String json = objectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(VEICULO_API)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect( status().isCreated() )
                .andExpect( jsonPath("id").value(10) )
                .andExpect( jsonPath("placa").value(dto.getPlaca()) )
                .andExpect( jsonPath("marca").value(dto.getMarca()) )
        ;

    }

    @Test
    @DisplayName("Deve lancar erro de validacao quando faltar dados na criacao do veiculo.")
    @WithMockUser(roles = "ADMIN")
    public void createInvalidVeiculoTest() throws Exception {

        String json = objectMapper().writeValueAsString(new VeiculoDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(VEICULO_API)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", Matchers.hasSize(Matchers.greaterThan(0))) );
    }

    @Test
    @DisplayName("Deve lancar erro ao tentar cadastrar veiculo com placa duplicada.")
    @WithMockUser(roles = "ADMIN")
    public void createVeiculoWithDuplicatedPlaca() throws Exception {

        VeiculoDTO dto = createNewVeiculo();
        String json = objectMapper().writeValueAsString(dto);
        String mensagemErro = "Placa já cadastrada.";
        BDDMockito.given(service.save(Mockito.any(Veiculo.class)))
                .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, mensagemErro));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(VEICULO_API)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform( request )
                .andExpect( status().isConflict() )
                .andExpect( jsonPath("errors", Matchers.hasSize(1)) )
                .andExpect( jsonPath("errors[0]").value(mensagemErro) );
    }

    @Test
    @DisplayName("Deve obter informacoes de um veiculo.")
    @WithMockUser(roles = "USER")
    public void getVeiculoDetailsTest() throws Exception {
        Long id = 1L;
        Veiculo veiculo = Veiculo.builder().id(id)
                .placa(createNewVeiculo().getPlaca())
                .marca(createNewVeiculo().getMarca())
                .ano(createNewVeiculo().getAno())
                .cor(createNewVeiculo().getCor())
                .precoUsd(createNewVeiculo().getPrecoUsd())
                .ativo(true).build();

        BDDMockito.given( service.getById(id) ).willReturn(Optional.of(veiculo));
        BDDMockito.given( exchangeRateService.getUsdToBrl() ).willReturn(new BigDecimal("5.00"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(VEICULO_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id) )
                .andExpect( jsonPath("placa").value(createNewVeiculo().getPlaca()) )
        ;
    }

    @Test
    @DisplayName("Deve retornar resource not found quando o veiculo procurado nao existir")
    @WithMockUser(roles = "USER")
    public void veiculoNotFoundTest() throws Exception {

        BDDMockito.given( service.getById(Mockito.anyLong()) ).willReturn( Optional.empty() );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(VEICULO_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect( status().isNotFound() );
    }

    @Test
    @DisplayName("Deve filtrar veiculos")
    @WithMockUser(roles = "USER")
    public void findVeiculosTest() throws Exception {

        Long id = 1L;
        Veiculo veiculo = Veiculo.builder().id(id)
                .placa("ABC1234").marca("Toyota").ano(2020).cor("Preto")
                .precoUsd(new BigDecimal("20000.00")).ativo(true).build();

        BDDMockito.given( service.find(Mockito.any(), Mockito.any(Pageable.class)) )
                .willReturn( new PageImpl<>(Arrays.asList(veiculo), PageRequest.of(0, 100), 1) );
        BDDMockito.given( exchangeRateService.getUsdToBrl() ).willReturn(new BigDecimal("5.00"));

        String queryString = "?marca=Toyota&page=0&size=100";

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(VEICULO_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform( request )
                .andExpect( status().isOk() )
                .andExpect( jsonPath("content", Matchers.hasSize(1)) )
        ;
    }

    @Test
    @DisplayName("Deve retornar 401 ao acessar veiculos sem autenticacao")
    public void unauthorizedTest() throws Exception {
        mvc.perform( MockMvcRequestBuilders.get(VEICULO_API).accept(MediaType.APPLICATION_JSON) )
                .andExpect( status().isUnauthorized() );
    }

    @Test
    @DisplayName("Deve atualizar veiculo com PUT")
    @WithMockUser(roles = "ADMIN")
    public void updateVeiculoTest() throws Exception {
        VeiculoDTO dto = createNewVeiculo();
        Veiculo updated = Veiculo.builder().id(3L).placa("ABC1234").marca("Honda").ano(2021).cor("Branco")
                .precoUsd(new BigDecimal("21000")).ativo(true).build();
        BDDMockito.given(service.update(Mockito.eq(3L), Mockito.any(Veiculo.class))).willReturn(updated);
        BDDMockito.given(exchangeRateService.getUsdToBrl()).willReturn(new BigDecimal("5"));

        mvc.perform(MockMvcRequestBuilders.put(VEICULO_API + "/3")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("marca").value("Honda"));
    }

    @Test
    @DisplayName("Deve aplicar PATCH em veiculo")
    @WithMockUser(roles = "ADMIN")
    public void patchVeiculoTest() throws Exception {
        Veiculo updated = Veiculo.builder().id(4L).placa("ABC1234").marca("Toyota").ano(2020).cor("Cinza")
                .precoUsd(new BigDecimal("19000")).ativo(true).build();
        BDDMockito.given(service.patch(Mockito.eq(4L), Mockito.any(com.example.car_manager_api.api.dto.VeiculoPatchDTO.class))).willReturn(updated);
        BDDMockito.given(exchangeRateService.getUsdToBrl()).willReturn(new BigDecimal("5"));

        String json = "{\"cor\":\"Cinza\"}";
        mvc.perform(MockMvcRequestBuilders.patch(VEICULO_API + "/4")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("cor").value("Cinza"));
    }

    @Test
    @DisplayName("Deve remover veiculo com DELETE")
    @WithMockUser(roles = "ADMIN")
    public void deleteVeiculoTest() throws Exception {
        Mockito.doNothing().when(service).delete(5L);

        mvc.perform(MockMvcRequestBuilders.delete(VEICULO_API + "/5")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve listar relatorio por marca")
    @WithMockUser(roles = "USER")
    public void relatorioPorMarcaTest() throws Exception {
        VeiculoRepository.MarcaContagem m = Mockito.mock(VeiculoRepository.MarcaContagem.class);
        BDDMockito.given(m.getMarca()).willReturn("VW");
        BDDMockito.given(m.getQuantidade()).willReturn(3L);
        BDDMockito.given(service.getRelatorioPorMarca()).willReturn(Collections.singletonList(m));

        mvc.perform(MockMvcRequestBuilders.get(VEICULO_API + "/relatorios/por-marca").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].marca").value("VW"))
                .andExpect(jsonPath("$[0].quantidade").value(3));
    }

    @Test
    @DisplayName("Deve baixar relatorio por marca em PDF")
    @WithMockUser(roles = "USER")
    public void relatorioPorMarcaPdfTest() throws Exception {
        byte[] pdf = "%PDF-1.4\n%\n".getBytes(StandardCharsets.US_ASCII);
        BDDMockito.given(relatorioService.gerarRelatorioPorMarca()).willReturn(pdf);

        mvc.perform(MockMvcRequestBuilders.get(VEICULO_API + "/relatorios/por-marca/pdf")
                        .accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("relatorio-marcas.pdf")))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(pdf));
    }

    @Test
    @DisplayName("Deve retornar 401 ao baixar PDF do relatorio sem autenticacao")
    public void relatorioPorMarcaPdfSemAuthTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(VEICULO_API + "/relatorios/por-marca/pdf")
                        .accept(MediaType.ALL))
                .andExpect(status().isUnauthorized());
    }

    private VeiculoDTO createNewVeiculo() {
        return VeiculoDTO.builder()
                .placa("ABC1234")
                .marca("Toyota")
                .ano(2020)
                .cor("Preto")
                .precoUsd(new BigDecimal("20000.00"))
                .build();
    }

    private ObjectMapper objectMapper() {
        return JsonMapper.builder().build();
    }

}
