package com.example.car_manager_api.service;

import com.example.car_manager_api.api.dto.RelatorioMarcaDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RelatorioService {

    private final VeiculoService veiculoService;
    private JasperReport relatorioMarcasTemplate;

    public RelatorioService(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @PostConstruct
    private void carregarTemplates() {
        try {
            InputStream is = getClass().getResourceAsStream("/reports/relatorio-marcas.jrxml");
            if (is == null) {
                throw new IllegalStateException("Template relatorio-marcas.jrxml não encontrado no classpath.");
            }
            relatorioMarcasTemplate = JasperCompileManager.compileReport(is);
            log.info(" template de relatório por marca carregado. ");
        } catch (JRException e) {
            throw new IllegalStateException("Erro ao compilar template do relatório.", e);
        }
    }

    public byte[] gerarRelatorioPorMarca() {
        List<RelatorioMarcaDTO> dados = veiculoService.getRelatorioPorMarca()
                .stream()
                .map(m -> RelatorioMarcaDTO.builder()
                        .marca(m.getMarca())
                        .quantidade(m.getQuantidade())
                        .build())
                .collect(Collectors.toList());

        try {
            JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(dados);
            JasperPrint print = JasperFillManager.fillReport(relatorioMarcasTemplate, new HashMap<>(), ds);
            return JasperExportManager.exportReportToPdf(print);
        } catch (JRException e) {
            throw new RuntimeException("Erro ao gerar relatório por marca.", e);
        }
    }

}
