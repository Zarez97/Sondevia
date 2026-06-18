package com.proyecto_bad115.sistema_encuestas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Confirmación del registro de datos personales (CU11).
 * El email identifica al participante en los siguientes pasos del flujo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipanteResponseDTO {
    private Integer idEncuesta;
    private String email;
    private String nombre;
}
