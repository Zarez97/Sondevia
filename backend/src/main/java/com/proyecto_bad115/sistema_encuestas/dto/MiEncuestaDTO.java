package com.proyecto_bad115.sistema_encuestas.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Etapa 18 - Encuesta del panel "Mis Encuestas" del encuestado:
 * sus respuestas en progreso (borradores) y respondidas (enviadas).
 */
@Getter
@Setter
public class MiEncuestaDTO {
    private Integer idEncuesta;
    private String tituloEncuesta;
    private String objetivoEncuesta;
    private String tokenPublico;
    private Integer estadoRespuesta;  // 1=BORRADOR, 2=ENVIADA
    private String estadoNombre;      // "En progreso" / "Respondida"
    private LocalDate fecha;          // actualización (borrador) o envío (respondida)
    private Integer numeroRegistro;   // idRespuesta (solo respondidas)
}
