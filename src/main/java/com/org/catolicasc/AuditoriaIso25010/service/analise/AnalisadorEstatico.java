package com.org.catolicasc.AuditoriaIso25010.service.analise;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.org.catolicasc.AuditoriaIso25010.model.ProjetoAlvo;
import com.org.catolicasc.AuditoriaIso25010.model.ResultadoModulo;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AnalisadorEstatico implements AnalisadorISO {

    // Marcação: Orquestrador da análise estática
    @Override
    public ResultadoModulo executarAnalise(ProjetoAlvo projeto) {
        ResultadoModulo resultado = new ResultadoModulo();
        resultado.setNomeModulo("Módulo I: Análise de Manutenibilidade (Estática)");
        
        Map<String, Object> metricas = new HashMap<>();
        int complexidadeTotal = 0;
        int acoplamentoTotal = 0;
        int duplicacaoTotal = 0;

        if (projeto.getDiretorioLocal() != null) {
            try (Stream<Path> paths = Files.walk(projeto.getDiretorioLocal())) {
                List<Path> arquivosJava = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .collect(Collectors.toList());

                List<String> todasAsLinhas = new ArrayList<>();

                for (Path arquivo : arquivosJava) {
                    try {
                        CompilationUnit cu = StaticJavaParser.parse(arquivo);
                        complexidadeTotal += calcularComplexidade(cu);
                        acoplamentoTotal += calcularAcoplamento(cu);
                        
                        todasAsLinhas.addAll(Files.readAllLines(arquivo));
                    } catch (Exception e) {
                        // Marcação: Ignorar arquivos corrompidos para garantir a resiliência do parser
                    }
                }
                
                duplicacaoTotal = calcularDuplicacao(todasAsLinhas);

            } catch (IOException e) {
                resultado.setAprovado(false);
                resultado.setObservacao("Erro crítico de I/O ao ler o diretório do projeto clonado.");
                return resultado;
            }
        }

        metricas.put("complexidadeCiclomatica", complexidadeTotal);
        metricas.put("acoplamentoObjetos", acoplamentoTotal);
        metricas.put("duplicacaoCodigo", duplicacaoTotal);
        
        resultado.setMetricas(metricas);
        
        // Marcação: Verificação do critério de aprovação do módulo
        if (complexidadeTotal > 150 || acoplamentoTotal > 100 || duplicacaoTotal > 10) {
            resultado.setAprovado(false);
            resultado.setObservacao("O código falha na ISO 25010 por excesso de complexidade estrutural, acoplamento entre classes ou duplicação excessiva.");
        } else {
            resultado.setAprovado(true);
            resultado.setObservacao("Aprovado com sucesso nos padrões estáticos de manutenibilidade.");
        }

        return resultado;
    }

    // Marcação: Cálculo da complexidade ciclomática baseada em ramificações condicionais
    private int calcularComplexidade(CompilationUnit cu) {
        List<IfStmt> condicoesIf = cu.findAll(IfStmt.class);
        int ifCount = condicoesIf.size();
        
        int elseCount = 0;
        for (IfStmt ifStmt : condicoesIf) {
            if (ifStmt.getElseStmt().isPresent()) {
                elseCount++;
            }
        }
        return ifCount + elseCount;
    }

    // Marcação: Cálculo do acoplamento CBO baseado nas dependências de tipo do arquivo
    private int calcularAcoplamento(CompilationUnit cu) {
        Set<String> tiposUnicos = new HashSet<>();
        List<ClassOrInterfaceType> tiposEncontrados = cu.findAll(ClassOrInterfaceType.class);
        
        for (ClassOrInterfaceType tipo : tiposEncontrados) {
            tiposUnicos.add(tipo.getNameAsString());
        }
        return tiposUnicos.size();
    }

    // Marcação: Identificação de DRY através de janela deslizante de 5 linhas
    private int calcularDuplicacao(List<String> linhas) {
        if (linhas.size() < 5) return 0;
        
        Map<String, Integer> contagemBlocos = new HashMap<>();
        int blocosDuplicados = 0;
        
        for (int i = 0; i <= linhas.size() - 5; i++) {
            String bloco = linhas.get(i).trim() + 
                           linhas.get(i + 1).trim() + 
                           linhas.get(i + 2).trim() + 
                           linhas.get(i + 3).trim() + 
                           linhas.get(i + 4).trim();
            
            // Marcação: Descartar blocos excessivamente curtos como chaves de fechamento
            if (bloco.length() > 15) { 
                contagemBlocos.put(bloco, contagemBlocos.getOrDefault(bloco, 0) + 1);
            }
        }
        
        for (Integer ocorrencias : contagemBlocos.values()) {
            if (ocorrencias > 1) {
                blocosDuplicados += (ocorrencias - 1);
            }
        }
        
        return blocosDuplicados;
    }
}