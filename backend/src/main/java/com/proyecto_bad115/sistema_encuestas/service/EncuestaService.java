package com.proyecto_bad115.sistema_encuestas.service;

import com.proyecto_bad115.sistema_encuestas.dto.EncuestaRequestDTO;
import com.proyecto_bad115.sistema_encuestas.dto.EncuestaResponseDTO;
import com.proyecto_bad115.sistema_encuestas.model.Encuesta;
import com.proyecto_bad115.sistema_encuestas.model.EstadoEncuesta;
import com.proyecto_bad115.sistema_encuestas.model.Usuario;
import com.proyecto_bad115.sistema_encuestas.repository.EncuestaRepository;
import com.proyecto_bad115.sistema_encuestas.repository.PreguntaRepository;
import com.proyecto_bad115.sistema_encuestas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EncuestaService {

    private final EncuestaRepository encuestaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PreguntaRepository preguntaRepository;

    public EncuestaService(EncuestaRepository encuestaRepository,
                           UsuarioRepository usuarioRepository,
                           PreguntaRepository preguntaRepository) {
        this.encuestaRepository = encuestaRepository;
        this.usuarioRepository = usuarioRepository;
        this.preguntaRepository = preguntaRepository;
    }

    public List<EncuestaResponseDTO> listarPorUsuario(String email) {
        return encuestaRepository.findByUsuarioEmailUser(email)
                .stream().map(this::toDTO).toList();
    }

    public List<EncuestaResponseDTO> listarTodas() {
        return encuestaRepository.findAll().stream().map(this::toDTO).toList();
    }

    public EncuestaResponseDTO buscarPorId(Integer id) {
        return encuestaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Encuesta no encontrada"));
    }

    public EncuestaResponseDTO crear(EncuestaRequestDTO dto, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmailUser(emailUsuario)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        Encuesta encuesta = new Encuesta();
        encuesta.setTituloEncuesta(dto.getTituloEncuesta());
        encuesta.setObjetivoEncuesta(dto.getObjetivoEncuesta());
        encuesta.setInstruccionesEncuesta(dto.getInstruccionesEncuesta());
        encuesta.setGrupoMeta(dto.getGrupoMeta());
        encuesta.setFechaCierre(dto.getFechaCierre());
        encuesta.setFechaCreacion(LocalDate.now());
        encuesta.setEstadoEncuesta(EstadoEncuesta.EN_DISENO);
        encuesta.setUsuario(usuario);

        return toDTO(encuestaRepository.save(encuesta));
    }

    public EncuestaResponseDTO actualizar(Integer id, EncuestaRequestDTO dto) {
        Encuesta encuesta = encuestaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Encuesta no encontrada"));

        if (encuesta.getEstadoEncuesta() != EstadoEncuesta.EN_DISENO) {
            throw new IllegalStateException("Solo se pueden editar encuestas en estado 'En diseño'");
        }

        encuesta.setTituloEncuesta(dto.getTituloEncuesta());
        encuesta.setObjetivoEncuesta(dto.getObjetivoEncuesta());
        encuesta.setInstruccionesEncuesta(dto.getInstruccionesEncuesta());
        encuesta.setGrupoMeta(dto.getGrupoMeta());
        encuesta.setFechaCierre(dto.getFechaCierre());

        return toDTO(encuestaRepository.save(encuesta));
    }

    public void eliminar(Integer id) {
        Encuesta encuesta = encuestaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Encuesta no encontrada"));

        if (encuesta.getEstadoEncuesta() != EstadoEncuesta.EN_DISENO) {
            throw new IllegalStateException("Solo se pueden eliminar encuestas en estado 'En diseño'");
        }
        encuestaRepository.deleteById(id);
    }

    private EncuestaResponseDTO toDTO(Encuesta e) {
        EncuestaResponseDTO dto = new EncuestaResponseDTO();
        dto.setIdEncuesta(e.getIdEncuesta());
        dto.setTituloEncuesta(e.getTituloEncuesta());
        dto.setObjetivoEncuesta(e.getObjetivoEncuesta());
        dto.setInstruccionesEncuesta(e.getInstruccionesEncuesta());
        dto.setGrupoMeta(e.getGrupoMeta());
        dto.setEstadoEncuesta(e.getEstadoEncuesta());
        dto.setEstadoNombre(nombreEstado(e.getEstadoEncuesta()));
        dto.setFechaCreacion(e.getFechaCreacion());
        dto.setFechaCierre(e.getFechaCierre());
        dto.setNombreUsuario(e.getUsuario().getNombreUser());
        dto.setTotalPreguntas(preguntaRepository.countByEncuestaIdEncuesta(e.getIdEncuesta()));
        return dto;
    }

    private String nombreEstado(Integer estado) {
        return switch (estado) {
            case EstadoEncuesta.EN_DISENO -> "En Diseño";
            case EstadoEncuesta.PUBLICADA -> "Publicada";
            case EstadoEncuesta.CERRADA -> "Cerrada";
            default -> "Desconocido";
        };
    }
}
