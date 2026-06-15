package com.proyecto_bad115.sistema_encuestas.controller;

import com.proyecto_bad115.sistema_encuestas.dto.RespuestaEnvioDTO;
import com.proyecto_bad115.sistema_encuestas.service.PublicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Acciones de respuesta que requieren autenticación: el encuestado se identifica
 * por su sesión (JWT). CU13 (envío) y verificación de si ya respondió.
 */
@RestController
@RequestMapping("/responder")
@CrossOrigin(origins = "*")
public class ResponderController {

    private final PublicoService publicoService;

    public ResponderController(PublicoService publicoService) {
        this.publicoService = publicoService;
    }

    @GetMapping("/{token}/estado")
    public ResponseEntity<?> estado(@PathVariable String token, @AuthenticationPrincipal String email) {
        try {
            return ResponseEntity.ok(Map.of(
                    "yaRespondido", publicoService.yaRespondio(token, email),
                    "tieneBorrador", publicoService.tieneBorrador(token, email)
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping("/{token}/borrador")
    public ResponseEntity<?> obtenerBorrador(@PathVariable String token, @AuthenticationPrincipal String email) {
        try {
            return ResponseEntity.ok(publicoService.obtenerBorrador(token, email));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping("/{token}/borrador")
    public ResponseEntity<?> guardarBorrador(@PathVariable String token,
                                             @AuthenticationPrincipal String email,
                                             @RequestBody RespuestaEnvioDTO dto) {
        try {
            publicoService.guardarBorrador(token, email, dto.getRespuestas());
            return ResponseEntity.ok(Map.of("mensaje", "Progreso guardado"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> enviar(@PathVariable String token,
                                    @AuthenticationPrincipal String email,
                                    @RequestBody RespuestaEnvioDTO dto) {
        try {
            return ResponseEntity.ok(publicoService.enviarRespuestas(token, email, dto.getRespuestas()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", e.getMessage()));
        }
    }
}
