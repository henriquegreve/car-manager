package com.example.car_manager_api.api.dto;

import lombok.*;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoDTO {

    private Long id;

    @NotEmpty
    @Pattern(regexp = "^[A-Za-z0-9-]{7}$", message = "Placa deve ter 7 caracteres alfanuméricos")
    private String placa;

    @NotEmpty
    private String marca;

    @NotNull
    private Integer ano;

    @NotEmpty
    private String cor;

    @NotNull
    private BigDecimal precoUsd;

    private BigDecimal precoBrlEstimado;

    private Boolean ativo;

    private Instant createdAt;

    private Instant updatedAt;

}
