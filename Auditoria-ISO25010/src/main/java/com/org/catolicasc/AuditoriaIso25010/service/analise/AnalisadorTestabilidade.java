package com.org.catolicasc.AuditoriaIso25010.service.analise;

import com.org.catolicasc.AuditoriaIso25010.model.ProjetoAlvo;
import com.org.catolicasc.AuditoriaIso25010.model.ResultadoModulo;
import org.apache.maven.shared.invoker.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class AnalisadorTestabilidade implements AnalisadorISO {

    @Override
    public ResultadoModulo executarAnalise(ProjetoAlvo projeto) {
        ResultadoModulo resultado = new ResultadoModulo();
        resultado.setNomeModulo("Módulo III: Confiabilidade (Testabilidade)");
        Map<String, Object> metricas = new HashMap<>();

        if (projeto.getDiretorioLocal() == null) {
            resultado.setAprovado(false);
            resultado.setObservacao("Diretório do projeto não encontrado.");
            return resultado;
        }
        
        // Marcação
        Optional<Path> pomPathOpt = Optional.empty();
        try (Stream<Path> paths = Files.walk(projeto.getDiretorioLocal())) {
            pomPathOpt = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals("pom.xml"))
                    .findFirst();
        } catch (Exception e) {
            // Ignora e continua com Optional.empty
        }

        boolean testesExecutadosComSucesso = false;
        if (pomPathOpt.isPresent()) {
            File diretorioMaven = pomPathOpt.get().getParent().toFile();
            testesExecutadosComSucesso = executarTestesMaven(diretorioMaven);
        } else {
            metricas.put("Aviso de Ambiente", "O arquivo pom.xml está ausente, execução de testes automatizada foi ignorada.");
        }
        
        long totalArquivosJava = contarArquivos(projeto.getDiretorioLocal(), false);
        long totalArquivosTeste = contarArquivos(projeto.getDiretorioLocal(), true);
        
        double proporcaoTestes = totalArquivosJava > 0 
                ? ((double) totalArquivosTeste / totalArquivosJava) * 100 
                : 0.0;

        metricas.put("Suíte de Testes Executada", testesExecutadosComSucesso ? "Sim" : "Não");
        metricas.put("Total de Arquivos de Produção", totalArquivosJava - totalArquivosTeste);
        metricas.put("Total de Arquivos de Teste", totalArquivosTeste);
        metricas.put("Cobertura Estimada", String.format("%.2f%%", proporcaoTestes));

        resultado.setMetricas(metricas);

        if (!testesExecutadosComSucesso && proporcaoTestes < 20.0) {
            resultado.setAprovado(false);
            resultado.setObservacao("O código falha na ISO 25010 por baixa testabilidade ou falha na execução da suíte de testes.");
        } else {
            resultado.setAprovado(true);
            resultado.setObservacao("Aprovado com sucesso. O sistema possui suíte de testes operante e proporção aceitável de cobertura.");
        }

        return resultado;
    }

    private boolean executarTestesMaven(File diretorioBase) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(diretorioBase, "pom.xml"));
        request.setGoals(Collections.singletonList("test"));
        request.setBatchMode(true);

        Invoker invoker = new DefaultInvoker();
        
        try {
            InvocationResult result = invoker.execute(request);
            return result.getExitCode() == 0;
        } catch (MavenInvocationException e) {
            return false;
        }
    }

    private long contarArquivos(Path diretorioBase, boolean isTeste) {
        try (Stream<Path> paths = Files.walk(diretorioBase)) {
            return paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .filter(p -> isTeste ? 
                                (p.toString().toLowerCase().contains("test")) : 
                                (!p.toString().toLowerCase().contains("test")))
                        .count();
        } catch (Exception e) {
            return 0;
        }
    }
}