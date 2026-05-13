package com.org.catolicasc.AuditoriaIso25010.model;

import lombok.Data;
import java.nio.file.Path;

@Data
public class ProjetoAlvo {

    private String urlGit;
    private Path diretorioLocal;
    private boolean clonadoComSucesso;
    private boolean compilouComSucesso;

}