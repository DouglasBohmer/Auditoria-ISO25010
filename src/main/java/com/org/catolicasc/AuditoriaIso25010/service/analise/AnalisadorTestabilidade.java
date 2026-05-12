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

        File diretorioBase = projeto.getDiretorioLocal().toFile();
        boolean testesExecutadosComSucesso = executarTestesMaven(diretorioBase);
        
        // Marcação: Fallback para calcular a proporção de arquivos de teste (útil se o projeto não tiver JaCoCo)
        long totalArquivosJava = contarArquivos(projeto.getDiretorioLocal(), "src/main/java");
        long totalArquivosTeste = contarArquivos(projeto.getDiretorioLocal(), "src/test/java");
        
        double proporcaoTestes = totalArquivosJava > 0 
                ? ((double) totalArquivosTeste / totalArquivosJava) * 100 
                : 0.0;

        metricas.put("buildDeTestesPassou", testesExecutadosComSucesso);
        metricas.put("totalArquivosProducao", totalArquivosJava);
        metricas.put("totalArquivosTeste", totalArquivosTeste);
        metricas.put("coberturaEstimada", String.format("%.2f%%", proporcaoTestes));

        resultado.setMetricas(metricas);

        if (!testesExecutadosComSucesso || proporcaoTestes < 20.0) {
            resultado.setAprovado(false);
            resultado.setObservacao("O código falha na ISO 25010 por baixa testabilidade ou falha na execução da suíte de testes.");
        } else {
            resultado.setAprovado(true);
            resultado.setObservacao("Aprovado com sucesso. O sistema possui suíte de testes operante e proporção aceitável de cobertura.");
        }

        return resultado;
    }

    // Marcação: Invocação programática do Maven
    private boolean executarTestesMaven(File diretorioBase) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(diretorioBase, "pom.xml"));
        request.setGoals(Collections.singletonList("test"));
        request.setBatchMode(true); // Evita que o Maven peça interações no console

        Invoker invoker = new DefaultInvoker();
        
        try {
            InvocationResult result = invoker.execute(request);
            return result.getExitCode() == 0;
        } catch (MavenInvocationException e) {
            return false;
        }
    }

    private long contarArquivos(Path diretorioBase, String subPasta) {
        Path caminhoCompleto = diretorioBase.resolve(subPasta);
        if (!Files.exists(caminhoCompleto)) return 0;

        try (Stream<Path> paths = Files.walk(caminhoCompleto)) {
            return paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .count();
        } catch (Exception e) {
            return 0;
        }
    }
}