package com.proyecto_bad115.sistema_encuestas.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EncuestaResponseDTO {
    private Integer idEncuesta;
    private String tituloEncuesta;
    private String objetivoEncuesta;
    private String instruccionesEncuesta;
    private String grupoMeta;
    private Integer estadoEncuesta;
    private String estadoNombre;
    private LocalDate fechaCreacion;
    private LocalDate fechaCierre;
    private String nombreUsuario;
    private Integer totalPreguntas;
}
