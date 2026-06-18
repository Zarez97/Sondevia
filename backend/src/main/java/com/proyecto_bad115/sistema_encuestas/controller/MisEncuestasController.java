package com.proyecto_bad115.sistema_encuestas.controller;

import com.proyecto_bad115.sistema_encuestas.dto.MiEncuestaDTO;
import com.proyecto_bad115.sistema_encuestas.service.PublicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Etapa 18 - Panel "Mis Encuestas" del encuestado autenticado:
 * lista sus respuestas en progreso (borradores) y respondidas (enviadas).
 */
@RestController
@RequestMapping("/mis-encuestas")
@CrossOrigin(origins = "*")
public class MisEncuestasController {

    private final PublicoService publicoService;

    public MisEncuestasController(PublicoService publicoService) {
        this.publicoService = publicoService;
    }

    @GetMapping
    public ResponseEntity<List<MiEncuestaDTO>> listar(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(publicoService.misEncuestas(email));
    }
}
