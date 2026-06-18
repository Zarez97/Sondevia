package com.proyecto_bad115.sistema_encuestas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolRequestDTO {

    @NotBlank
    private String nombreRol;

    private String descripcionRol;
}
