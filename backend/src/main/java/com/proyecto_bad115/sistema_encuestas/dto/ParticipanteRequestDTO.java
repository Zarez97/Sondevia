package com.proyecto_bad115.sistema_encuestas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Datos personales del encuestado (CU11).
 * El correo electrónico actúa como identificador único del participante.
 */
@Getter
@Setter
public class ParticipanteRequestDTO {

    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private LocalDate fechaNacimiento;
}
