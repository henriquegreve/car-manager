package com.example.car_manager_api.api.resource;

import com.example.car_manager_api.api.dto.RelatorioMarcaDTO;
import com.example.car_manager_api.api.dto.VeiculoDTO;
import com.example.car_manager_api.api.dto.VeiculoFilterDTO;
import com.example.car_manager_api.api.dto.VeiculoPatchDTO;
import com.example.car_manager_api.model.entity.Veiculo;
import com.example.car_manager_api.model.repository.VeiculoRepository;
import com.example.car_manager_api.service.ExchangeRateService;
import com.example.car_manager_api.service.RelatorioService;
import com.example.car_manager_api.service.VeiculoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
@Slf4j
public class VeiculoController {

    private final VeiculoService service;
    private final ExchangeRateService exchangeRateService;
    private final RelatorioService relatorioService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public VeiculoDTO create( @RequestBody @Valid VeiculoDTO dto ){
        log.info(" creating a vehicle for placa: {} ", dto.getPlaca());
        Veiculo entity = modelMapper.map( dto, Veiculo.class );
        entity = service.save(entity);
        return toDto(entity);
    }

    @GetMapping("{id}")
    public VeiculoDTO get( @PathVariable Long id ){
        log.info(" obtaining details for vehicle id: {} ", id);
        return service
                .getById(id)
                .map( this::toDto )
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id){
        log.info(" deleting vehicle of id: {} ", id);
        service.delete(id);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public VeiculoDTO update( @PathVariable Long id, @RequestBody @Valid VeiculoDTO dto){
        log.info(" updating vehicle of id: {} ", id);
        Veiculo dados = modelMapper.map(dto, Veiculo.class);
        Veiculo updated = service.update(id, dados);
        return toDto(updated);
    }

    @PatchMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public VeiculoDTO patch(@PathVariable Long id, @RequestBody VeiculoPatchDTO patch){
        log.info(" patching vehicle of id: {} ", id);
        Veiculo updated = service.patch(id, patch);
        return toDto(updated);
    }

    @GetMapping
    public Page<VeiculoDTO> find( VeiculoFilterDTO filter, Pageable pageRequest ){
        Page<Veiculo> result = service.find(filter, pageRequest);
        List<VeiculoDTO> list = result.getContent()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<VeiculoDTO>( list, pageRequest, result.getTotalElements() );
    }

    @GetMapping({"/relatorios/por-marca", "/relatorios/marca"})
    public List<RelatorioMarcaDTO> relatorioPorMarca(){
        return service.getRelatorioPorMarca().stream()
                .map( m -> RelatorioMarcaDTO.builder().marca(m.getMarca()).quantidade(m.getQuantidade()).build() )
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/relatorios/por-marca/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> relatorioPorMarcaPdf(){
        log.info(" gerando relatorio de veiculos por marca em PDF ");
        byte[] pdf = relatorioService.gerarRelatorioPorMarca();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=relatorio-marcas.pdf")
                .body(pdf);
    }

    private VeiculoDTO toDto(Veiculo veiculo){
        VeiculoDTO dto = modelMapper.map(veiculo, VeiculoDTO.class);
        if(veiculo.getPrecoUsd() != null){
            BigDecimal cotacao = exchangeRateService.getUsdToBrl();
            dto.setPrecoBrlEstimado( veiculo.getPrecoUsd().multiply(cotacao).setScale(2, RoundingMode.HALF_UP) );
        }
        return dto;
    }

}
