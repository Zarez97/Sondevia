package com.proyecto_bad115.sistema_encuestas.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreguntaResponseDTO {
    private Integer idPregunta;
    private String descripcionPregunta;
    private Boolean obligatoriaPregunta;
    private String tipoPregunta;
    private String tipoPreguntaCerrada;
    private Boolean esMixta;
    private Integer idEncuesta;
    private Integer orden;
}
