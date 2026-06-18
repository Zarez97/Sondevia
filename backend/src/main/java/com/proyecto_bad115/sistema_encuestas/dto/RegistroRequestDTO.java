package com.proyecto_bad115.sistema_encuestas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Auto-registro de un encuestado (CU14).
 * Crea una cuenta real con rol ENCUESTADO y contraseña propia.
 */
@Getter
@Setter
public class RegistroRequestDTO {

    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String contrasenia;

    @NotNull
    private LocalDate fechaNacimiento;
}
