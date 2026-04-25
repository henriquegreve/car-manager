package com.example.car_manager_api.model.repository;

import com.example.car_manager_api.model.entity.Veiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class VeiculoRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    VeiculoRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir veiculo ativo com a placa informada")
    public void returnTrueWhenPlacaExists(){
        String placa = "ABC1234";
        Veiculo veiculo = createNewVeiculo(placa);
        entityManager.persist(veiculo);

        boolean exists = repository.existsByPlacaIgnoreCaseAndAtivoTrue(placa);

        assertThat(exists).isTrue();
    }

    public static Veiculo createNewVeiculo(String placa) {
        return Veiculo.builder()
                .placa(placa)
                .marca("Toyota")
                .ano(2020)
                .cor("Preto")
                .precoUsd(new BigDecimal("20000.00"))
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve retornar false quando nao existir veiculo ativo com a placa informada")
    public void returnFalseWhenPlacaDoesntExist(){
        String placa = "ABC1234";

        boolean exists = repository.existsByPlacaIgnoreCaseAndAtivoTrue(placa);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um veiculo ativo por id")
    public void findByIdAndAtivoTrueTest(){
        Veiculo veiculo = createNewVeiculo("ABC1234");
        entityManager.persist(veiculo);

        Optional<Veiculo> found = repository.findByIdAndAtivoTrue(veiculo.getId());

        assertThat(found.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um veiculo")
    public void saveVeiculoTest(){
        Veiculo veiculo = createNewVeiculo("ABC1234");

        Veiculo saved = repository.save(veiculo);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar a contagem agrupada por marca")
    public void contagemPorMarcaTest(){
        entityManager.persist(createNewVeiculo("AAA1111"));

        Veiculo outra = createNewVeiculo("BBB2222");
        outra.setMarca("Honda");
        entityManager.persist(outra);

        Veiculo terceira = createNewVeiculo("CCC3333");
        terceira.setMarca("Honda");
        entityManager.persist(terceira);

        List<VeiculoRepository.MarcaContagem> result = repository.contagemPorMarca();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(VeiculoRepository.MarcaContagem::getMarca)
                .contains("Toyota", "Honda");
    }

    @Test
    @DisplayName("Deve filtrar veiculos pelas propriedades")
    public void findFiltradoTest(){
        Veiculo veiculo = createNewVeiculo("ABC1234");
        entityManager.persist(veiculo);

        Veiculo outro = createNewVeiculo("XYZ9999");
        outro.setMarca("Honda");
        outro.setCor("Branco");
        outro.setAno(2018);
        outro.setPrecoUsd(new BigDecimal("15000.00"));
        entityManager.persist(outro);

        Page<Veiculo> result = repository.findFiltrado(
                "Toyota", null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPlaca()).isEqualTo("ABC1234");
    }

    @Test
    @DisplayName("Deve atualizar updated_at ao modificar entidade persistida")
    public void preUpdateAtualizaTimestampTest() throws InterruptedException {
        Veiculo v = createNewVeiculo("UPD1234");
        entityManager.persistAndFlush(v);
        Instant antes = v.getUpdatedAt();

        Thread.sleep(15);
        v.setCor("Azul");
        repository.save(v);
        entityManager.flush();
        entityManager.clear();

        Veiculo recarregado = repository.findById(v.getId()).orElseThrow();
        assertThat(recarregado.getUpdatedAt()).isAfter(antes);
    }

}
