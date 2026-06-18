package com.proyecto_bad115.sistema_encuestas.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpcionResponseDTO {
    private Integer idOpcionRespuesta;
    private String textoOpcion;
    private Integer valorNumerico;
    private Boolean esMixta;
}
