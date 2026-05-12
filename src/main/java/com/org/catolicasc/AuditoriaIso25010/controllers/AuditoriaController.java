package com.org.catolicasc.AuditoriaIso25010.controllers;

import com.org.catolicasc.AuditoriaIso25010.dto.AuditoriaRequestDTO;
import com.org.catolicasc.AuditoriaIso25010.model.ResultadoModulo;
import com.org.catolicasc.AuditoriaIso25010.service.AuditoriaService;
import com.org.catolicasc.AuditoriaIso25010.service.relatorio.RelatorioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;
    private final RelatorioService relatorioService;

    @Autowired
    public AuditoriaController(AuditoriaService auditoriaService, RelatorioService relatorioService) {
        this.auditoriaService = auditoriaService;
        this.relatorioService = relatorioService;
    }

    // Marcação: Endpoint para retornar JSON cru (diagnóstico em tempo real)
    @PostMapping("/executar")
    public ResponseEntity<List<ResultadoModulo>> iniciarAuditoria(@Valid @RequestBody AuditoriaRequestDTO request) {
        List<ResultadoModulo> resultado = auditoriaService.processarAuditoria(request);
        return ResponseEntity.ok(resultado);
    }

    // Marcação: Endpoint exclusivo para gerar e baixar o Relatório PDF
    @PostMapping("/relatorio")
    public ResponseEntity<byte[]> baixarRelatorioPdf(@Valid @RequestBody AuditoriaRequestDTO request) {
        List<ResultadoModulo> resultados = auditoriaService.processarAuditoria(request);
        byte[] pdfBytes = relatorioService.gerarRelatorioPdf(resultados);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Auditoria_ISO25010.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}