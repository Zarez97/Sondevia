package com.proyecto_bad115.sistema_encuestas.controller;

import com.proyecto_bad115.sistema_encuestas.dto.RolRequestDTO;
import com.proyecto_bad115.sistema_encuestas.dto.RolResponseDTO;
import com.proyecto_bad115.sistema_encuestas.service.RolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/roles")
@CrossOrigin(origins = "*")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> listar() {
        return ResponseEntity.ok(rolService.listarRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(rolService.buscarPorId(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody RolRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crearRol(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @Valid @RequestBody RolRequestDTO dto) {
        try {
            return ResponseEntity.ok(rolService.actualizarRol(id, dto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            rolService.eliminarRol(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{id}/privilegios")
    public ResponseEntity<?> asignarPrivilegios(@PathVariable Integer id, @RequestBody List<Integer> privilegioIds) {
        try {
            return ResponseEntity.ok(rolService.asignarPrivilegios(id, privilegioIds));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping("/{rolId}/usuarios/{usuarioId}")
    public ResponseEntity<?> asignarRolAUsuario(@PathVariable Integer rolId, @PathVariable Integer usuarioId) {
        try {
            return ResponseEntity.ok(rolService.asignarRolAUsuario(usuarioId, rolId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/{rolId}/usuarios/{usuarioId}")
    public ResponseEntity<?> quitarRolAUsuario(@PathVariable Integer rolId, @PathVariable Integer usuarioId) {
        try {
            rolService.quitarRolAUsuario(usuarioId, rolId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        }
    }
}
