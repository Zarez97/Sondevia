package com.proyecto_bad115.sistema_encuestas.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RolResponseDTO {
    private Integer idRol;
    private String nombreRol;
    private String descripcionRol;
    private List<PrivilegioResponseDTO> privilegios;
}
