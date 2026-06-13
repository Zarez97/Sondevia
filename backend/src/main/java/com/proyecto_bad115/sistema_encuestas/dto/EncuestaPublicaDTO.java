package com.proyecto_bad115.sistema_encuestas.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Datos de una encuesta visibles para el encuestado (flujo público).
 * No expone información interna (creador, token, etc.).
 */
@Getter
@Setter
public class EncuestaPublicaDTO {
    private Integer idEncuesta;
    private String tituloEncuesta;
    private String objetivoEncuesta;
    private String instruccionesEncuesta;
    private String grupoMeta;
    private LocalDate fechaCierre;
    private Integer totalPreguntas;
}
