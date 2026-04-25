package com.example.car_manager_api.service;

import com.example.car_manager_api.api.dto.VeiculoFilterDTO;
import com.example.car_manager_api.api.dto.VeiculoPatchDTO;
import com.example.car_manager_api.exception.BusinessException;
import com.example.car_manager_api.model.entity.Veiculo;
import com.example.car_manager_api.model.repository.VeiculoRepository;
import com.example.car_manager_api.service.impl.VeiculoServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class VeiculoServiceTest {

    VeiculoService service;

    @MockitoBean
    VeiculoRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new VeiculoServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um veiculo")
    public void saveVeiculoTest() {
        Veiculo veiculo = createValidVeiculo();
        when( repository.existsByPlacaIgnoreCaseAndAtivoTrue(Mockito.anyString()) ).thenReturn(false);
        when( repository.save(veiculo) ).thenReturn(
                Veiculo.builder().id(1L)
                        .placa("ABC1234")
                        .marca("Toyota")
                        .ano(2020)
                        .cor("Preto")
                        .precoUsd(new BigDecimal("20000.00"))
                        .ativo(true)
                        .build()
        );

        Veiculo saved = service.save(veiculo);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPlaca()).isEqualTo("ABC1234");
        assertThat(saved.getMarca()).isEqualTo("Toyota");
    }

    private Veiculo createValidVeiculo() {
        return Veiculo.builder()
                .placa("ABC1234")
                .marca("Toyota")
                .ano(2020)
                .cor("Preto")
                .precoUsd(new BigDecimal("20000.00"))
                .build();
    }

    @Test
    @DisplayName("Deve lancar erro de negocio ao tentar salvar veiculo com placa duplicada")
    public void shouldNotSaveVeiculoWithDuplicatedPlaca(){
        Veiculo veiculo = createValidVeiculo();
        when( repository.existsByPlacaIgnoreCaseAndAtivoTrue(Mockito.anyString()) ).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(veiculo));

        assertThat(exception)
                .isInstanceOf(ResponseStatusException.class);
        assertThat(((ResponseStatusException) exception).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        Mockito.verify(repository, Mockito.never()).save(veiculo);
    }

    @Test
    @DisplayName("Deve obter um veiculo por id")
    public void getByIdTest(){
        Long id = 1L;
        Veiculo veiculo = createValidVeiculo();
        veiculo.setId(id);

        when( repository.findByIdAndAtivoTrue(id) ).thenReturn(Optional.of(veiculo));

        Optional<Veiculo> found = service.getById(id);

        assertThat( found.isPresent() ).isTrue();
        assertThat( found.get().getId() ).isEqualTo(id);
        assertThat( found.get().getPlaca() ).isEqualTo(veiculo.getPlaca());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar veiculo inexistente")
    public void veiculoNotFoundByIdTest(){
        Long id = 1L;
        when( repository.findByIdAndAtivoTrue(id) ).thenReturn(Optional.empty());

        Optional<Veiculo> veiculo = service.getById(id);

        assertThat( veiculo.isPresent() ).isFalse();
    }

    @Test
    @DisplayName("Deve realizar exclusao logica de um veiculo")
    public void deleteVeiculoTest(){
        Long id = 1L;
        Veiculo veiculo = createValidVeiculo();
        veiculo.setId(id);

        when( repository.findByIdAndAtivoTrue(id) ).thenReturn(Optional.of(veiculo));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow( () -> service.delete(id) );

        assertThat(veiculo.getAtivo()).isFalse();
        Mockito.verify(repository, Mockito.times(1)).save(veiculo);
    }

    @Test
    @DisplayName("Deve atualizar um veiculo")
    public void updateVeiculoTest(){
        Long id = 1L;
        Veiculo existing = createValidVeiculo();
        existing.setId(id);

        Veiculo dados = Veiculo.builder()
                .placa("XYZ9999").marca("Honda").ano(2021).cor("Branco")
                .precoUsd(new BigDecimal("25000.00")).build();

        when( repository.findByIdAndAtivoTrue(id) ).thenReturn(Optional.of(existing));
        when( repository.findByPlacaIgnoreCaseAndAtivoTrue("XYZ9999") ).thenReturn(Optional.empty());
        when( repository.save(existing) ).thenReturn(existing);

        Veiculo updated = service.update(id, dados);

        assertThat(updated.getPlaca()).isEqualTo("XYZ9999");
        assertThat(updated.getMarca()).isEqualTo("Honda");
        assertThat(updated.getAno()).isEqualTo(2021);
        assertThat(updated.getCor()).isEqualTo("Branco");
    }

    @Test
    @DisplayName("Deve lancar erro ao tentar patch sem nenhum campo")
    public void patchSemCampoTest(){
        VeiculoPatchDTO patch = VeiculoPatchDTO.builder().build();

        Throwable exception = Assertions.catchThrowable(() -> service.patch(1L, patch));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Informe ao menos um campo para atualização.");
    }

    @Test
    @DisplayName("Deve aplicar patch parcial em um veiculo")
    public void patchVeiculoTest(){
        Long id = 1L;
        Veiculo existing = createValidVeiculo();
        existing.setId(id);

        VeiculoPatchDTO patch = VeiculoPatchDTO.builder()
                .cor("Vermelho")
                .precoUsd(new BigDecimal("18000.00"))
                .build();

        when( repository.findByIdAndAtivoTrue(id) ).thenReturn(Optional.of(existing));
        when( repository.save(existing) ).thenReturn(existing);

        Veiculo updated = service.patch(id, patch);

        assertThat(updated.getCor()).isEqualTo("Vermelho");
        assertThat(updated.getPrecoUsd()).isEqualByComparingTo("18000.00");
        assertThat(updated.getMarca()).isEqualTo("Toyota");
    }

    @Test
    @DisplayName("Deve filtrar veiculos pelas propriedades")
    public void findVeiculosTest(){
        VeiculoFilterDTO filter = VeiculoFilterDTO.builder().marca("Toyota").build();

        Veiculo veiculo = createValidVeiculo();
        veiculo.setId(1L);
        Pageable pageRequest = PageRequest.of(0, 10);
        List<Veiculo> lista = Arrays.asList(veiculo);

        Page<Veiculo> page = new PageImpl<>(lista, pageRequest, 1);
        when( repository.findFiltrado(
                Mockito.anyString(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(Pageable.class))
        ).thenReturn(page);

        Page<Veiculo> result = service.find(filter, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve rejeitar filtro com minPreco maior que maxPreco")
    public void findPrecoInvalidoTest() {
        VeiculoFilterDTO filter = VeiculoFilterDTO.builder()
                .minPreco(new BigDecimal("50000"))
                .maxPreco(new BigDecimal("10000"))
                .build();

        Throwable ex = Assertions.catchThrowable(() -> service.find(filter, PageRequest.of(0, 10)));

        assertThat(ex).isInstanceOf(BusinessException.class)
                .hasMessageContaining("minPreco");
    }

    @Test
    @DisplayName("Deve retornar relatorio por marca")
    public void relatorioPorMarcaTest() {
        VeiculoRepository.MarcaContagem row = Mockito.mock(VeiculoRepository.MarcaContagem.class);
        when(row.getMarca()).thenReturn("Ford");
        when(row.getQuantidade()).thenReturn(2L);
        when(repository.contagemPorMarca()).thenReturn(Arrays.asList(row));

        List<VeiculoRepository.MarcaContagem> list = service.getRelatorioPorMarca();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getMarca()).isEqualTo("Ford");
        assertThat(list.get(0).getQuantidade()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve falhar update com placa duplicada de outro veiculo")
    public void updatePlacaDuplicadaTest() {
        Long id = 1L;
        Veiculo existing = createValidVeiculo();
        existing.setId(id);

        Veiculo dados = Veiculo.builder()
                .placa("OUTRA12")
                .marca("Honda").ano(2021).cor("Branco")
                .precoUsd(new BigDecimal("10000")).build();

        Veiculo outro = Veiculo.builder().id(99L).placa("OUTRA12").build();

        when(repository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(existing));
        when(repository.findByPlacaIgnoreCaseAndAtivoTrue("OUTRA12")).thenReturn(Optional.of(outro));

        Throwable ex = Assertions.catchThrowable(() -> service.update(id, dados));

        assertThat(ex).isInstanceOf(ResponseStatusException.class);
        assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

}
