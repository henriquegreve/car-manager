package com.example.car_manager_api.service;

import com.example.car_manager_api.api.dto.VeiculoFilterDTO;
import com.example.car_manager_api.api.dto.VeiculoPatchDTO;
import com.example.car_manager_api.model.entity.Veiculo;
import com.example.car_manager_api.model.repository.VeiculoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface VeiculoService {

    Veiculo save( Veiculo veiculo );

    Optional<Veiculo> getById(Long id);

    Veiculo update(Long id, Veiculo veiculo);

    Veiculo patch(Long id, VeiculoPatchDTO patch);

    void delete(Long id);

    Page<Veiculo> find( VeiculoFilterDTO filter, Pageable pageRequest );

    List<VeiculoRepository.MarcaContagem> getRelatorioPorMarca();

}
