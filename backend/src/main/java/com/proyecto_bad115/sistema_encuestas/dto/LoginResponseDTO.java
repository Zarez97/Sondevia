package com.proyecto_bad115.sistema_encuestas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
    private String nombreUser;
    private String emailUser;
}
