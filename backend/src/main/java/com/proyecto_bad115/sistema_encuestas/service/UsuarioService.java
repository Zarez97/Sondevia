package com.proyecto_bad115.sistema_encuestas.service;

import com.proyecto_bad115.sistema_encuestas.config.PasswordPolicyValidator;
import com.proyecto_bad115.sistema_encuestas.dto.ActualizarUsuarioDTO;
import com.proyecto_bad115.sistema_encuestas.dto.CrearUsuarioDTO;
import com.proyecto_bad115.sistema_encuestas.dto.UsuarioResponseDTO;
import com.proyecto_bad115.sistema_encuestas.model.EstadoUsuario;
import com.proyecto_bad115.sistema_encuestas.model.Usuario;
import com.proyecto_bad115.sistema_encuestas.repository.EncuestaRepository;
import com.proyecto_bad115.sistema_encuestas.repository.RespuestaRepository;
import com.proyecto_bad115.sistema_encuestas.repository.UsuarioRepository;
import com.proyecto_bad115.sistema_encuestas.repository.UsuarioRolRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final EncuestaRepository encuestaRepository;
    private final RespuestaRepository respuestaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          UsuarioRolRepository usuarioRolRepository,
                          EncuestaRepository encuestaRepository,
                          RespuestaRepository respuestaRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.encuestaRepository = encuestaRepository;
        this.respuestaRepository = respuestaRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioRepository.findUsuariosConRoles().stream()
                .map(this::toDTOFromView)
                .toList();
    }

    public UsuarioResponseDTO buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    public UsuarioResponseDTO crearUsuario(CrearUsuarioDTO dto) {
        if (usuarioRepository.existsByEmailUser(dto.getEmailUser())) {
            throw new IllegalArgumentException("El correo ya esta registrado");
        }
        PasswordPolicyValidator.validate(dto.getContraseniaUser());

        Usuario usuario = new Usuario();
        usuario.setNombreUser(dto.getNombreUser());
        usuario.setEmailUser(dto.getEmailUser());
        usuario.setContraseniaUser(passwordEncoder.encode(dto.getContraseniaUser()));
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setEstadoUser(EstadoUsuario.ACTIVO);
        usuario.setIntentosFallidos(0);

        usuarioRepository.save(usuario);

        emailService.enviarBienvenida(dto.getEmailUser(), dto.getNombreUser(), dto.getContraseniaUser());

        return toDTO(usuario);
    }

    public UsuarioResponseDTO actualizarUsuario(Integer id, ActualizarUsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        usuario.setNombreUser(dto.getNombreUser());
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuarioRepository.save(usuario);
        return toDTO(usuario);
    }

    public UsuarioResponseDTO activarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        usuario.setEstadoUser(EstadoUsuario.ACTIVO);
        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);
        return toDTO(usuario);
    }

    public UsuarioResponseDTO darDeBaja(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        usuario.setEstadoUser(EstadoUsuario.INACTIVO);
        usuarioRepository.save(usuario);
        return toDTO(usuario);
    }

    public UsuarioResponseDTO desbloquearUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        try {
            usuarioRepository.callDesbloquearUsuario(id);
        } catch (DataAccessException e) {
            throw new IllegalStateException(mensajeProcedure(e));
        }

        emailService.enviarDesbloqueo(usuario.getEmailUser(), usuario.getNombreUser());

        return toDTO(usuarioRepository.findById(id).orElseThrow());
    }

    private String mensajeProcedure(DataAccessException e) {
        Throwable raiz = e;
        while (raiz.getCause() != null) raiz = raiz.getCause();
        String msg = raiz.getMessage();
        if (msg == null) return "Error al procesar la operación";
        int inicio = msg.indexOf("ERROR: ");
        if (inicio >= 0) {
            msg = msg.substring(inicio + 7);
            int fin = msg.indexOf('\n');
            if (fin > 0) msg = msg.substring(0, fin).trim();
        }
        return msg;
    }

    /**
     * Eliminación segura: bloquea si el usuario es dueño de encuestas o tiene
     * respuestas registradas (para no romper integridad), y no permite que un
     * usuario se elimine a sí mismo. Limpia primero los vínculos de roles.
     */
    @Transactional
    public void eliminarUsuario(Integer id, String emailSolicitante) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        if (emailSolicitante != null && usuario.getEmailUser().equalsIgnoreCase(emailSolicitante)) {
            throw new IllegalStateException("No puedes eliminar tu propia cuenta.");
        }
        if (encuestaRepository.existsByUsuarioIdUser(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar: el usuario tiene encuestas creadas. " +
                    "Reasigna o elimina esas encuestas, o da de baja al usuario.");
        }
        if (respuestaRepository.existsByUsuarioIdUser(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar: el usuario tiene respuestas registradas en encuestas. " +
                    "Considera darlo de baja en su lugar.");
        }

        usuarioRolRepository.deleteAll(usuarioRolRepository.findByUsuarioIdUser(id));
        usuarioRepository.delete(usuario);
    }

    // Columnas de v_usuarios_roles:
    // 0:idUser 1:nombre 2:email 3:fechaNacimiento 4:estado 5:intentos 6:roles(csv)
    private UsuarioResponseDTO toDTOFromView(Object[] row) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setIdUser((Integer) row[0]);
        dto.setNombreUser((String) row[1]);
        dto.setEmailUser((String) row[2]);
        dto.setFechaNacimiento(toLocalDate(row[3]));
        dto.setEstadoUser((Integer) row[4]);
        dto.setIntentosFallidos((Integer) row[5]);
        String rolesStr = (String) row[6];
        dto.setRoles(rolesStr != null ? Arrays.asList(rolesStr.split(", ")) : List.of());
        return dto;
    }

    private LocalDate toLocalDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDate ld) return ld;
        if (obj instanceof java.sql.Date d) return d.toLocalDate();
        return null;
    }

    private UsuarioResponseDTO toDTO(Usuario u) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setIdUser(u.getIdUser());
        dto.setNombreUser(u.getNombreUser());
        dto.setEmailUser(u.getEmailUser());
        dto.setFechaNacimiento(u.getFechaNacimiento());
        dto.setEstadoUser(u.getEstadoUser());
        dto.setIntentosFallidos(u.getIntentosFallidos());
        dto.setRoles(usuarioRolRepository.findByUsuario(u).stream()
                .map(ur -> ur.getRol().getNombreRol())
                .toList());
        return dto;
    }
}
