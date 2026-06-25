package com.proyecto_bad115.sistema_encuestas.controller;

import com.proyecto_bad115.sistema_encuestas.dto.PreguntaRequestDTO;
import com.proyecto_bad115.sistema_encuestas.dto.PreguntaResponseDTO;
import com.proyecto_bad115.sistema_encuestas.service.PreguntaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/encuestas/{idEncuesta}/preguntas")
@CrossOrigin(origins = "*")
public class PreguntaController {

    private final PreguntaService preguntaService;

    public PreguntaController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    @GetMapping
    public ResponseEntity<List<PreguntaResponseDTO>> listar(@PathVariable Integer idEncuesta) {
        return ResponseEntity.ok(preguntaService.listarPorEncuesta(idEncuesta));
    }

    @PostMapping
    public ResponseEntity<?> agregar(@PathVariable Integer idEncuesta,
                                     @Valid @RequestBody PreguntaRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(preguntaService.agregar(idEncuesta, dto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{idPregunta}")
    public ResponseEntity<?> actualizar(@PathVariable Integer idEncuesta,
                                        @PathVariable Integer idPregunta,
                                        @Valid @RequestBody PreguntaRequestDTO dto) {
        try {
            return ResponseEntity.ok(preguntaService.actualizar(idPregunta, dto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/{idPregunta}")
    public ResponseEntity<?> eliminar(@PathVariable Integer idEncuesta,
                                      @PathVariable Integer idPregunta) {
        try {
            preguntaService.eliminar(idPregunta);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", e.getMessage()));
        }
    }
}
