package com.org.catolicasc.AuditoriaIso25010.model;

import lombok.Data;
import java.util.Map;

@Data
public class ResultadoModulo {

    private String nomeModulo;
    private boolean aprovado;
    private Map<String, Object> metricas;
    private String observacao;

}