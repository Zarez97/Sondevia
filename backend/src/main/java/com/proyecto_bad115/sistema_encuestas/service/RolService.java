package com.proyecto_bad115.sistema_encuestas.service;

import com.proyecto_bad115.sistema_encuestas.dto.PrivilegioResponseDTO;
import com.proyecto_bad115.sistema_encuestas.dto.RolRequestDTO;
import com.proyecto_bad115.sistema_encuestas.dto.RolResponseDTO;
import com.proyecto_bad115.sistema_encuestas.model.*;
import com.proyecto_bad115.sistema_encuestas.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final PrivilegioRepository privilegioRepository;
    private final RolPrivilegioRepository rolPrivilegioRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    public RolService(RolRepository rolRepository,
                      PrivilegioRepository privilegioRepository,
                      RolPrivilegioRepository rolPrivilegioRepository,
                      UsuarioRepository usuarioRepository,
                      UsuarioRolRepository usuarioRolRepository) {
        this.rolRepository = rolRepository;
        this.privilegioRepository = privilegioRepository;
        this.rolPrivilegioRepository = rolPrivilegioRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    public List<RolResponseDTO> listarRoles() {
        return rolRepository.findAll().stream().map(this::toDTO).toList();
    }

    public RolResponseDTO buscarPorId(Integer id) {
        return rolRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Rol no encontrado"));
    }

    public RolResponseDTO crearRol(RolRequestDTO dto) {
        if (rolRepository.existsByNombreRol(dto.getNombreRol())) {
            throw new IllegalArgumentException("Ya existe un rol con ese nombre");
        }
        Rol rol = new Rol();
        rol.setNombreRol(dto.getNombreRol().toUpperCase());
        rol.setDescripcionRol(dto.getDescripcionRol());
        return toDTO(rolRepository.save(rol));
    }

    public RolResponseDTO actualizarRol(Integer id, RolRequestDTO dto) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rol no encontrado"));
        rol.setNombreRol(dto.getNombreRol().toUpperCase());
        rol.setDescripcionRol(dto.getDescripcionRol());
        return toDTO(rolRepository.save(rol));
    }

    public void eliminarRol(Integer id) {
        if (!rolRepository.existsById(id)) throw new NoSuchElementException("Rol no encontrado");
        rolPrivilegioRepository.findByRolIdRol(id).forEach(rolPrivilegioRepository::delete);
        usuarioRolRepository.findByUsuarioIdUser(id).forEach(usuarioRolRepository::delete);
        rolRepository.deleteById(id);
    }

    public RolResponseDTO asignarPrivilegios(Integer rolId, List<Integer> privilegioIds) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new NoSuchElementException("Rol no encontrado"));

        rolPrivilegioRepository.findByRolIdRol(rolId).forEach(rolPrivilegioRepository::delete);

        privilegioIds.forEach(privId ->
            privilegioRepository.findById(privId).ifPresent(priv -> {
                RolPrivilegio rp = new RolPrivilegio();
                rp.setRol(rol);
                rp.setPrivilegio(priv);
                rolPrivilegioRepository.save(rp);
            })
        );
        return toDTO(rol);
    }

    public RolResponseDTO asignarRolAUsuario(Integer usuarioId, Integer rolId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new NoSuchElementException("Rol no encontrado"));

        boolean yaAsignado = usuarioRolRepository.findByUsuario(usuario)
                .stream().anyMatch(ur -> ur.getRol().getIdRol().equals(rolId));

        if (!yaAsignado) {
            UsuarioRol ur = new UsuarioRol();
            ur.setUsuario(usuario);
            ur.setRol(rol);
            usuarioRolRepository.save(ur);
        }
        return toDTO(rol);
    }

    public void quitarRolAUsuario(Integer usuarioId, Integer rolId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        usuarioRolRepository.findByUsuario(usuario).stream()
                .filter(ur -> ur.getRol().getIdRol().equals(rolId))
                .findFirst()
                .ifPresent(usuarioRolRepository::delete);
    }

    private RolResponseDTO toDTO(Rol rol) {
        RolResponseDTO dto = new RolResponseDTO();
        dto.setIdRol(rol.getIdRol());
        dto.setNombreRol(rol.getNombreRol());
        dto.setDescripcionRol(rol.getDescripcionRol());
        dto.setPrivilegios(rolPrivilegioRepository.findByRolIdRol(rol.getIdRol())
                .stream().map(rp -> {
                    PrivilegioResponseDTO p = new PrivilegioResponseDTO();
                    p.setIdPrivilegio(rp.getPrivilegio().getIdPrivilegio());
                    p.setNombrePrivilegio(rp.getPrivilegio().getNombrePrivilegio());
                    p.setDescripcionPrivilegio(rp.getPrivilegio().getDescripcionPrivilegio());
                    p.setUrlPrivilegio(rp.getPrivilegio().getUrlPrivilegio());
                    return p;
                }).toList());
        return dto;
    }
}
