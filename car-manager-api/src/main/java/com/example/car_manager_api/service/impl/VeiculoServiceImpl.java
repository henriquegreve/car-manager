package com.example.car_manager_api.service.impl;

import com.example.car_manager_api.api.dto.VeiculoFilterDTO;
import com.example.car_manager_api.api.dto.VeiculoPatchDTO;
import com.example.car_manager_api.exception.BusinessException;
import com.example.car_manager_api.model.entity.Veiculo;
import com.example.car_manager_api.model.repository.VeiculoRepository;
import com.example.car_manager_api.service.VeiculoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class VeiculoServiceImpl implements VeiculoService {

    private VeiculoRepository repository;

    public VeiculoServiceImpl(VeiculoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Veiculo save(Veiculo veiculo) {
        String placa = normalizarPlaca(veiculo.getPlaca());
        if( repository.existsByPlacaIgnoreCaseAndAtivoTrue(placa) ){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Placa já cadastrada.");
        }
        veiculo.setPlaca(placa);
        veiculo.setAtivo(true);
        return repository.save(veiculo);
    }

    @Override
    public Optional<Veiculo> getById(Long id) {
        return repository.findByIdAndAtivoTrue(id);
    }

    @Override
    public Veiculo update(Long id, Veiculo dados) {
        Veiculo veiculo = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String placa = normalizarPlaca(dados.getPlaca());
        validarPlacaDuplicada(placa, id);

        veiculo.setPlaca(placa);
        veiculo.setMarca(dados.getMarca());
        veiculo.setAno(dados.getAno());
        veiculo.setCor(dados.getCor());
        veiculo.setPrecoUsd(dados.getPrecoUsd());

        return repository.save(veiculo);
    }

    @Override
    public Veiculo patch(Long id, VeiculoPatchDTO patch) {
        if( !temAlgumCampo(patch) ){
            throw new BusinessException("Informe ao menos um campo para atualização.");
        }

        Veiculo veiculo = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if( patch.getPlaca() != null ){
            String placa = normalizarPlaca(patch.getPlaca());
            validarPlacaDuplicada(placa, id);
            veiculo.setPlaca(placa);
        }
        if( patch.getMarca() != null ){
            veiculo.setMarca(patch.getMarca());
        }
        if( patch.getAno() != null ){
            veiculo.setAno(patch.getAno());
        }
        if( patch.getCor() != null ){
            veiculo.setCor(patch.getCor());
        }
        if( patch.getPrecoUsd() != null ){
            if( patch.getPrecoUsd().compareTo(BigDecimal.ZERO) <= 0 ){
                throw new BusinessException("Preço em USD deve ser positivo.");
            }
            veiculo.setPrecoUsd(patch.getPrecoUsd());
        }

        return repository.save(veiculo);
    }

    @Override
    public void delete(Long id) {
        Veiculo veiculo = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        veiculo.setAtivo(false);
        repository.save(veiculo);
    }

    @Override
    public Page<Veiculo> find( VeiculoFilterDTO filter, Pageable pageRequest ) {
        if( filter.getMinPreco() != null && filter.getMaxPreco() != null
                && filter.getMinPreco().compareTo(filter.getMaxPreco()) > 0 ){
            throw new BusinessException("minPreco não pode ser maior que maxPreco.");
        }
        return repository.findFiltrado(
                filter.getMarca(),
                filter.getAno(),
                filter.getCor(),
                filter.getMinPreco(),
                filter.getMaxPreco(),
                pageRequest
        );
    }

    @Override
    public List<VeiculoRepository.MarcaContagem> getRelatorioPorMarca() {
        return repository.contagemPorMarca();
    }

    private void validarPlacaDuplicada(String placa, Long id) {
        repository.findByPlacaIgnoreCaseAndAtivoTrue(placa).ifPresent( outro -> {
            if( !outro.getId().equals(id) ){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Placa já cadastrada.");
            }
        });
    }

    private boolean temAlgumCampo(VeiculoPatchDTO patch){
        return patch.getPlaca() != null
                || patch.getMarca() != null
                || patch.getAno() != null
                || patch.getCor() != null
                || patch.getPrecoUsd() != null;
    }

    private String normalizarPlaca(String placa){
        if(placa == null) return null;
        return placa.trim().replace(" ", "").toUpperCase(Locale.ROOT);
    }

}
