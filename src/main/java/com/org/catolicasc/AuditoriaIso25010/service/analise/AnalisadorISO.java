package com.org.catolicasc.AuditoriaIso25010.service.analise;

import com.org.catolicasc.AuditoriaIso25010.model.ProjetoAlvo;
import com.org.catolicasc.AuditoriaIso25010.model.ResultadoModulo;

public interface AnalisadorISO {

    ResultadoModulo executarAnalise(ProjetoAlvo projeto);

}