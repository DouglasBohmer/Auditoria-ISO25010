package com.org.catolicasc.AuditoriaIso25010.service;

import com.org.catolicasc.AuditoriaIso25010.model.ProjetoAlvo;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class GitService {

    // Marcação: Método para clonar o repositório
    public ProjetoAlvo clonarRepositorio(String urlRepositorio) {
        ProjetoAlvo projeto = new ProjetoAlvo();
        projeto.setUrlGit(urlRepositorio);

        try {
            Path diretorioTemporario = Files.createTempDirectory("auditoria-" + UUID.randomUUID().toString());
            File repoDir = diretorioTemporario.toFile();

            Git.cloneRepository()
                    .setURI(urlRepositorio)
                    .setDirectory(repoDir)
                    .call();

            projeto.setDiretorioLocal(diretorioTemporario);
            projeto.setClonadoComSucesso(true);

        } catch (Exception e) {
            projeto.setClonadoComSucesso(false);
            // Marcação: Tratamento de erro na clonagem
        }

        return projeto;
    }

    // Marcação: Método para deletar os arquivos após a análise
    public void limparWorkspace(Path diretorio) {
        if (diretorio != null) {
            FileSystemUtils.deleteRecursively(diretorio.toFile());
        }
    }
}