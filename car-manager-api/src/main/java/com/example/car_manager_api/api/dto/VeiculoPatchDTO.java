package com.example.car_manager_api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoPatchDTO {

    private String placa;
    private String marca;
    private Integer ano;
    private String cor;
    private BigDecimal precoUsd;

}
