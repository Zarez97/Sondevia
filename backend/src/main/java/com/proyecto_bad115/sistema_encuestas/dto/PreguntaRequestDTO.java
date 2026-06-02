package com.proyecto_bad115.sistema_encuestas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreguntaRequestDTO {

    @NotBlank
    private String descripcionPregunta;

    @NotNull
    private Boolean obligatoriaPregunta;

    @NotBlank
    private String tipoPregunta;
}
