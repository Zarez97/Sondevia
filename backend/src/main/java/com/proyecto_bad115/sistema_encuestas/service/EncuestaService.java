package com.proyecto_bad115.sistema_encuestas.service;

import com.proyecto_bad115.sistema_encuestas.dto.EncuestaRequestDTO;
import com.proyecto_bad115.sistema_encuestas.dto.EncuestaResponseDTO;
import com.proyecto_bad115.sistema_encuestas.model.Encuesta;
import com.proyecto_bad115.sistema_encuestas.model.EstadoEncuesta;
import com.proyecto_bad115.sistema_encuestas.model.Usuario;
import com.proyecto_bad115.sistema_encuestas.repository.EncuestaRepository;
import com.proyecto_bad115.sistema_encuestas.repository.PreguntaRepository;
import com.proyecto_bad115.sistema_encuestas.repository.UsuarioRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

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
        return encuestaRepository.findResumenEncuestasByUsuario(email)
                .stream().map(this::toDTOFromView).toList();
    }

    public List<EncuestaResponseDTO> listarTodas() {
        return encuestaRepository.findResumenTodasEncuestas()
                .stream().map(this::toDTOFromView).toList();
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

    /**
     * CU08 - Publica una encuesta delegando validaciones y cambio de estado
     * al procedure sp_publicar_encuesta (atomicidad garantizada en BD).
     */
    public EncuestaResponseDTO publicar(Integer id) {
        Encuesta encuesta = encuestaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Encuesta no encontrada"));

        String token = isBlank(encuesta.getTokenPublico())
                ? UUID.randomUUID().toString().replace("-", "")
                : encuesta.getTokenPublico();

        try {
            encuestaRepository.callPublicarEncuesta(id, token);
        } catch (DataAccessException e) {
            throw new IllegalStateException(mensajeProcedure(e));
        }

        return toDTO(encuestaRepository.findById(id).orElseThrow());
    }

    /**
     * Cierra una encuesta publicada delegando la validación y cambio de estado
     * al procedure sp_cerrar_encuesta.
     */
    public EncuestaResponseDTO cerrar(Integer id) {
        try {
            encuestaRepository.callCerrarEncuesta(id);
        } catch (DataAccessException e) {
            throw new IllegalStateException(mensajeProcedure(e));
        }
        return toDTO(encuestaRepository.findById(id).orElseThrow());
    }

    // Columnas de v_resumen_encuestas:
    // 0:id 1:titulo 2:objetivo 3:instrucciones 4:grupoMeta 5:estado
    // 6:fechaCreacion 7:fechaCierre 8:token 9:nombreUsuario 10:emailUsuario
    // 11:totalPreguntas 12:totalRespuestas
    private EncuestaResponseDTO toDTOFromView(Object[] row) {
        EncuestaResponseDTO dto = new EncuestaResponseDTO();
        dto.setIdEncuesta((Integer) row[0]);
        dto.setTituloEncuesta((String) row[1]);
        dto.setObjetivoEncuesta((String) row[2]);
        dto.setInstruccionesEncuesta((String) row[3]);
        dto.setGrupoMeta((String) row[4]);
        dto.setEstadoEncuesta((Integer) row[5]);
        dto.setEstadoNombre(nombreEstado((Integer) row[5]));
        dto.setFechaCreacion(toLocalDate(row[6]));
        dto.setFechaCierre(toLocalDate(row[7]));
        dto.setTokenPublico((String) row[8]);
        dto.setNombreUsuario((String) row[9]);
        dto.setTotalPreguntas(row[11] != null ? ((Number) row[11]).intValue() : 0);
        dto.setTotalRespuestas(row[12] != null ? ((Number) row[12]).longValue() : 0L);
        return dto;
    }

    private LocalDate toLocalDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDate ld) return ld;
        if (obj instanceof java.sql.Date d) return d.toLocalDate();
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
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
        dto.setTokenPublico(e.getTokenPublico());
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
