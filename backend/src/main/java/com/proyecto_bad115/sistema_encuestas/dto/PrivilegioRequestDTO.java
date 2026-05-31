package com.proyecto_bad115.sistema_encuestas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrivilegioRequestDTO {

    @NotBlank
    private String nombrePrivilegio;

    private String descripcionPrivilegio;

    @NotBlank
    private String urlPrivilegio;
}
