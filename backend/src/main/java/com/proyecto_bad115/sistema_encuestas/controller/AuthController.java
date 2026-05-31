package com.proyecto_bad115.sistema_encuestas.controller;

import com.proyecto_bad115.sistema_encuestas.dto.LoginRequestDTO;
import com.proyecto_bad115.sistema_encuestas.dto.LoginResponseDTO;
import com.proyecto_bad115.sistema_encuestas.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
