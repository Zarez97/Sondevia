package com.proyecto_bad115.sistema_encuestas.controller;

import com.proyecto_bad115.sistema_encuestas.dto.LoginRequestDTO;
import com.proyecto_bad115.sistema_encuestas.dto.LoginResponseDTO;
import com.proyecto_bad115.sistema_encuestas.dto.MenuItemDTO;
import com.proyecto_bad115.sistema_encuestas.dto.RegistroRequestDTO;
import com.proyecto_bad115.sistema_encuestas.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of("mensaje", e.getMessage()));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", e.getMessage()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@Valid @RequestBody RegistroRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping("/menu")
    public ResponseEntity<List<MenuItemDTO>> menu(Principal principal) {
        return ResponseEntity.ok(authService.obtenerMenu(principal.getName()));
    }
}
