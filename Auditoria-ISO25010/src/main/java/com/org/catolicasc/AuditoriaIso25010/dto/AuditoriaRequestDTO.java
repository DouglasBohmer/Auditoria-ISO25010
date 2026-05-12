package com.org.catolicasc.AuditoriaIso25010.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuditoriaRequestDTO {

    @NotBlank
    private String urlRepositorio;

}