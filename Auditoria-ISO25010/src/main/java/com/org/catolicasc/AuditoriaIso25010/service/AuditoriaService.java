package com.org.catolicasc.AuditoriaIso25010.service;

import com.org.catolicasc.AuditoriaIso25010.dto.AuditoriaRequestDTO;
import com.org.catolicasc.AuditoriaIso25010.model.ProjetoAlvo;
import com.org.catolicasc.AuditoriaIso25010.model.ResultadoModulo;
import com.org.catolicasc.AuditoriaIso25010.service.analise.AnalisadorISO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuditoriaService {

    private final GitService gitService;
    private final List<AnalisadorISO> analisadores;

    @Autowired
    public AuditoriaService(GitService gitService, List<AnalisadorISO> analisadores) {
        this.gitService = gitService;
        this.analisadores = analisadores;
    }

    // Marcação: Orquestrador principal do pipeline de execução
    public List<ResultadoModulo> processarAuditoria(AuditoriaRequestDTO request) {
        
        ProjetoAlvo projeto = gitService.clonarRepositorio(request.getUrlRepositorio());
        List<ResultadoModulo> resultados = new ArrayList<>();

        if (projeto.isClonadoComSucesso()) {
            for (AnalisadorISO analisador : analisadores) {
                resultados.add(analisador.executarAnalise(projeto));
            }
            
            gitService.limparWorkspace(projeto.getDiretorioLocal());
        }

        return resultados;
    }
}