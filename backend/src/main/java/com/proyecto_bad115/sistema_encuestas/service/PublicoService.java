package com.proyecto_bad115.sistema_encuestas.service;

import com.proyecto_bad115.sistema_encuestas.dto.EncuestaPublicaDTO;
import com.proyecto_bad115.sistema_encuestas.dto.ParticipanteRequestDTO;
import com.proyecto_bad115.sistema_encuestas.dto.ParticipanteResponseDTO;
import com.proyecto_bad115.sistema_encuestas.dto.PreguntaResponseDTO;
import com.proyecto_bad115.sistema_encuestas.model.*;
import com.proyecto_bad115.sistema_encuestas.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Flujo público del encuestado (sin autenticación). CU11-CU13.
 */
@Service
public class PublicoService {

    private static final String REGEX_EMAIL = "^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$";

    private final EncuestaRepository encuestaRepository;
    private final PreguntaRepository preguntaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RespuestaRepository respuestaRepository;
    private final PreguntaService preguntaService;
    private final PasswordEncoder passwordEncoder;

    public PublicoService(EncuestaRepository encuestaRepository,
                          PreguntaRepository preguntaRepository,
                          UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          UsuarioRolRepository usuarioRolRepository,
                          RespuestaRepository respuestaRepository,
                          PreguntaService preguntaService,
                          PasswordEncoder passwordEncoder) {
        this.encuestaRepository = encuestaRepository;
        this.preguntaRepository = preguntaRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.respuestaRepository = respuestaRepository;
        this.preguntaService = preguntaService;
        this.passwordEncoder = passwordEncoder;
    }

    /** Carga la encuesta vigente a partir del token (pantalla de bienvenida). */
    public EncuestaPublicaDTO cargarEncuesta(String token) {
        return toPublicaDTO(obtenerVigente(token));
    }

    /** CU12 - Carga las preguntas de la encuesta vigente para responderla. */
    public List<PreguntaResponseDTO> cargarPreguntas(String token) {
        Encuesta encuesta = obtenerVigente(token);
        return preguntaService.listarPorEncuesta(encuesta.getIdEncuesta());
    }

    /** CU11 - Registra los datos personales del participante. */
    @Transactional
    public ParticipanteResponseDTO registrarParticipante(String token, ParticipanteRequestDTO dto) {
        Encuesta encuesta = obtenerVigente(token);
        String email = normalizar(dto.getEmail());
        validarDatos(dto, email);

        if (respuestaRepository.existsByEncuestaIdEncuestaAndUsuarioEmailUser(encuesta.getIdEncuesta(), email)) {
            throw new IllegalStateException("Este correo electrónico ya respondió esta encuesta");
        }

        Usuario usuario = usuarioRepository.findByEmailUser(email)
                .map(existente -> {
                    existente.setNombreUser(dto.getNombre().trim());
                    existente.setFechaNacimiento(dto.getFechaNacimiento());
                    return usuarioRepository.save(existente);
                })
                .orElseGet(() -> crearEncuestado(dto, email));

        return new ParticipanteResponseDTO(encuesta.getIdEncuesta(), usuario.getEmailUser(), usuario.getNombreUser());
    }

    // ── Helpers ──────────────────────────────────────────────

    private Encuesta obtenerVigente(String token) {
        Encuesta encuesta = encuestaRepository.findByTokenPublico(token)
                .orElseThrow(() -> new NoSuchElementException("La encuesta no existe o el enlace no es válido"));

        if (encuesta.getEstadoEncuesta() == null || encuesta.getEstadoEncuesta() != EstadoEncuesta.PUBLICADA) {
            throw new IllegalStateException("Esta encuesta no está disponible para responder");
        }
        if (encuesta.getFechaCierre() != null && encuesta.getFechaCierre().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Esta encuesta ya cerró y no permite nuevas respuestas");
        }
        return encuesta;
    }

    private void validarDatos(ParticipanteRequestDTO dto, String email) {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (!email.matches(REGEX_EMAIL)) {
            throw new IllegalArgumentException("El correo electrónico no es válido");
        }
        if (dto.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        if (dto.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }
    }

    private Usuario crearEncuestado(ParticipanteRequestDTO dto, String email) {
        Usuario usuario = new Usuario();
        usuario.setNombreUser(dto.getNombre().trim());
        usuario.setEmailUser(email);
        usuario.setContraseniaUser(passwordEncoder.encode(UUID.randomUUID().toString())); // sin login
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setEstadoUser(EstadoUsuario.ACTIVO);
        usuario.setIntentosFallidos(0);
        Usuario guardado = usuarioRepository.save(usuario);

        rolRepository.findByNombreRol("ENCUESTADO").ifPresent(rol -> {
            UsuarioRol ur = new UsuarioRol();
            ur.setUsuario(guardado);
            ur.setRol(rol);
            usuarioRolRepository.save(ur);
        });
        return guardado;
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private EncuestaPublicaDTO toPublicaDTO(Encuesta e) {
        EncuestaPublicaDTO dto = new EncuestaPublicaDTO();
        dto.setIdEncuesta(e.getIdEncuesta());
        dto.setTituloEncuesta(e.getTituloEncuesta());
        dto.setObjetivoEncuesta(e.getObjetivoEncuesta());
        dto.setInstruccionesEncuesta(e.getInstruccionesEncuesta());
        dto.setGrupoMeta(e.getGrupoMeta());
        dto.setFechaCierre(e.getFechaCierre());
        dto.setTotalPreguntas(preguntaRepository.countByEncuestaIdEncuesta(e.getIdEncuesta()));
        return dto;
    }
}
