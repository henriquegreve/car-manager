package com.example.car_manager_api.model.repository;

import com.example.car_manager_api.model.entity.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Optional<Veiculo> findByIdAndAtivoTrue( Long id );

    Optional<Veiculo> findByPlacaIgnoreCaseAndAtivoTrue( String placa );

    boolean existsByPlacaIgnoreCaseAndAtivoTrue( String placa );

    @Query( value = " select v from Veiculo v where v.ativo = true " +
            " and ( :marca is null or lower(v.marca) = lower(:marca) ) " +
            " and ( :ano is null or v.ano = :ano ) " +
            " and ( :cor is null or lower(v.cor) = lower(:cor) ) " +
            " and ( :minPreco is null or v.precoUsd >= :minPreco ) " +
            " and ( :maxPreco is null or v.precoUsd <= :maxPreco ) ")
    Page<Veiculo> findFiltrado(
            @Param("marca") String marca,
            @Param("ano") Integer ano,
            @Param("cor") String cor,
            @Param("minPreco") BigDecimal minPreco,
            @Param("maxPreco") BigDecimal maxPreco,
            Pageable pageable
    );

    @Query(" select v.marca as marca, count(v) as quantidade from Veiculo v " +
            " where v.ativo = true group by v.marca order by v.marca ")
    List<MarcaContagem> contagemPorMarca();

    interface MarcaContagem {
        String getMarca();
        Long getQuantidade();
    }

}
