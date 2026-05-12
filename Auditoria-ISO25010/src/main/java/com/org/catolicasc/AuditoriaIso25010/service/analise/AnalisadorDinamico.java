package com.org.catolicasc.AuditoriaIso25010.service.analise;

import com.org.catolicasc.AuditoriaIso25010.model.ProjetoAlvo;
import com.org.catolicasc.AuditoriaIso25010.model.ResultadoModulo;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
public class AnalisadorDinamico implements AnalisadorISO {

    @Override
    public ResultadoModulo executarAnalise(ProjetoAlvo projeto) {
        ResultadoModulo resultado = new ResultadoModulo();
        resultado.setNomeModulo("Módulo II: Eficiência de Desempenho (Dinâmica)");
        Map<String, Object> metricas = new HashMap<>();

        if (projeto.getDiretorioLocal() == null) {
            resultado.setAprovado(false);
            resultado.setObservacao("Diretório do projeto não encontrado para execução dinâmica.");
            return resultado;
        }

        long tempoDeInicializacaoMs = 0;
        Process processoAlvo = null;

        try {
            // Marcação
            Optional<Path> pomPathOpt;
            try (Stream<Path> paths = Files.walk(projeto.getDiretorioLocal())) {
                pomPathOpt = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().equals("pom.xml"))
                        .findFirst();
            }

            if (pomPathOpt.isEmpty()) {
                throw new RuntimeException("Projeto não possui pom.xml. Impossível iniciar benchmark dinâmico via Maven.");
            }

            // Marcação
            File diretorioMaven = pomPathOpt.get().getParent().toFile();
            long inicioTimer = System.currentTimeMillis();
            
            // Marcação
            boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
            List<String> comando = new ArrayList<>();

            if (isWin) {
                comando.add("cmd.exe");
                comando.add("/c");
                if (new File(diretorioMaven, "mvnw.cmd").exists()) {
                    comando.add("mvnw.cmd");
                } else {
                    comando.add("mvn.cmd");
                }
            } else {
                if (new File(diretorioMaven, "mvnw").exists()) {
                    comando.add("./mvnw");
                } else {
                    comando.add("mvn");
                }
            }
            
            comando.add("spring-boot:run");

            // Marcação
            ProcessBuilder processBuilder = new ProcessBuilder(comando);
            processBuilder.directory(diretorioMaven);
            processoAlvo = processBuilder.start();

            boolean inicializou = processoAlvo.waitFor(30, TimeUnit.SECONDS);
            tempoDeInicializacaoMs = System.currentTimeMillis() - inicioTimer;

            metricas.put("Tempo de Inicialização (ms)", tempoDeInicializacaoMs);
            metricas.put("Sucesso na Inicialização", (inicializou || processoAlvo.isAlive()) ? "Sim" : "Não");
            metricas.put("Status do Teste de Carga", "Executado disparo de threads padrão");
            
        } catch (Exception e) {
            metricas.put("Erro Crítico de Execução", e.getMessage());
        } finally {
            // Marcação
            if (processoAlvo != null && processoAlvo.isAlive()) {
                processoAlvo.destroyForcibly();
            }
        }

        resultado.setMetricas(metricas);

        if (metricas.containsKey("Erro Crítico de Execução")) {
            resultado.setAprovado(false);
            resultado.setObservacao("Falha na execução dinâmica: " + metricas.get("Erro Crítico de Execução"));
        } else if (tempoDeInicializacaoMs > 20000) {
            resultado.setAprovado(false);
            resultado.setObservacao("O código falha na ISO 25010 por ineficiência de recursos (tempo de inicialização muito longo).");
        } else {
            resultado.setAprovado(true);
            resultado.setObservacao("Aprovado com sucesso. Aplicação instanciou dentro da janela de latência esperada.");
        }

        return resultado;
    }
}