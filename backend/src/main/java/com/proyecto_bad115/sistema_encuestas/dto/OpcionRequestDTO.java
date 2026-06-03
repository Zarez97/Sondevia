package com.proyecto_bad115.sistema_encuestas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpcionRequestDTO {

    @NotBlank
    private String textoOpcion;
}
