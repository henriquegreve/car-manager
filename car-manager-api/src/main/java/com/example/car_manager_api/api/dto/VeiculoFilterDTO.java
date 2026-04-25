package com.example.car_manager_api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VeiculoFilterDTO {

    private String marca;
    private Integer ano;
    private String cor;
    private BigDecimal minPreco;
    private BigDecimal maxPreco;

}
