package com.example.car_manager_api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelatorioMarcaDTO {

    private String marca;
    private Long quantidade;

}
