package com.proyecto_bad115.sistema_encuestas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Solicitud de desbloqueo de cuenta enviada desde el login por un usuario bloqueado. */
@Getter
@Setter
public class SolicitudDesbloqueoDTO {

    @NotBlank
    @Email
    private String email;
}
