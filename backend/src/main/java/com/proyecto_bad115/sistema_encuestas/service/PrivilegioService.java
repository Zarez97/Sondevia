package com.proyecto_bad115.sistema_encuestas.service;

import com.proyecto_bad115.sistema_encuestas.dto.PrivilegioRequestDTO;
import com.proyecto_bad115.sistema_encuestas.dto.PrivilegioResponseDTO;
import com.proyecto_bad115.sistema_encuestas.model.Privilegio;
import com.proyecto_bad115.sistema_encuestas.repository.PrivilegioRepository;
import com.proyecto_bad115.sistema_encuestas.repository.RolPrivilegioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PrivilegioService {

    private final PrivilegioRepository privilegioRepository;
    private final RolPrivilegioRepository rolPrivilegioRepository;

    public PrivilegioService(PrivilegioRepository privilegioRepository,
                             RolPrivilegioRepository rolPrivilegioRepository) {
        this.privilegioRepository = privilegioRepository;
        this.rolPrivilegioRepository = rolPrivilegioRepository;
    }

    public List<PrivilegioResponseDTO> listarPrivilegios() {
        return privilegioRepository.findAll().stream().map(this::toDTO).toList();
    }

    public PrivilegioResponseDTO buscarPorId(Integer id) {
        return privilegioRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Privilegio no encontrado"));
    }

    public PrivilegioResponseDTO crearPrivilegio(PrivilegioRequestDTO dto) {
        if (privilegioRepository.existsByNombrePrivilegio(dto.getNombrePrivilegio())) {
            throw new IllegalArgumentException("Ya existe un privilegio con ese nombre");
        }
        Privilegio p = new Privilegio();
        p.setNombrePrivilegio(dto.getNombrePrivilegio());
        p.setDescripcionPrivilegio(dto.getDescripcionPrivilegio());
        p.setUrlPrivilegio(dto.getUrlPrivilegio());
        return toDTO(privilegioRepository.save(p));
    }

    public PrivilegioResponseDTO actualizarPrivilegio(Integer id, PrivilegioRequestDTO dto) {
        Privilegio p = privilegioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Privilegio no encontrado"));
        p.setNombrePrivilegio(dto.getNombrePrivilegio());
        p.setDescripcionPrivilegio(dto.getDescripcionPrivilegio());
        p.setUrlPrivilegio(dto.getUrlPrivilegio());
        return toDTO(privilegioRepository.save(p));
    }

    public void eliminarPrivilegio(Integer id) {
        if (!privilegioRepository.existsById(id)) throw new NoSuchElementException("Privilegio no encontrado");
        rolPrivilegioRepository.findByRolIdRol(id).forEach(rolPrivilegioRepository::delete);
        privilegioRepository.deleteById(id);
    }

    private PrivilegioResponseDTO toDTO(Privilegio p) {
        PrivilegioResponseDTO dto = new PrivilegioResponseDTO();
        dto.setIdPrivilegio(p.getIdPrivilegio());
        dto.setNombrePrivilegio(p.getNombrePrivilegio());
        dto.setDescripcionPrivilegio(p.getDescripcionPrivilegio());
        dto.setUrlPrivilegio(p.getUrlPrivilegio());
        return dto;
    }
}
