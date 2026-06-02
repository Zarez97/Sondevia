package com.proyecto_bad115.sistema_encuestas.service;

import com.proyecto_bad115.sistema_encuestas.dto.PreguntaRequestDTO;
import com.proyecto_bad115.sistema_encuestas.dto.PreguntaResponseDTO;
import com.proyecto_bad115.sistema_encuestas.model.*;
import com.proyecto_bad115.sistema_encuestas.repository.EncuestaRepository;
import com.proyecto_bad115.sistema_encuestas.repository.PreguntaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final EncuestaRepository encuestaRepository;

    public PreguntaService(PreguntaRepository preguntaRepository,
                           EncuestaRepository encuestaRepository) {
        this.preguntaRepository = preguntaRepository;
        this.encuestaRepository = encuestaRepository;
    }

    public List<PreguntaResponseDTO> listarPorEncuesta(Integer idEncuesta) {
        return preguntaRepository.findByEncuestaIdEncuesta(idEncuesta)
                .stream().map(this::toDTO).toList();
    }

    public PreguntaResponseDTO agregar(Integer idEncuesta, PreguntaRequestDTO dto) {
        Encuesta encuesta = encuestaRepository.findById(idEncuesta)
                .orElseThrow(() -> new NoSuchElementException("Encuesta no encontrada"));

        if (encuesta.getEstadoEncuesta() != EstadoEncuesta.EN_DISENO) {
            throw new IllegalStateException("Solo se pueden agregar preguntas a encuestas en estado 'En diseño'");
        }

        Pregunta pregunta = new Pregunta();
        pregunta.setDescripcionPregunta(dto.getDescripcionPregunta());
        pregunta.setObligatoriaPregunta(dto.getObligatoriaPregunta());
        pregunta.setTipoPregunta(TipoPregunta.valueOf(dto.getTipoPregunta()));
        pregunta.setEsMixta(false);
        pregunta.setEncuesta(encuesta);

        return toDTO(preguntaRepository.save(pregunta));
    }

    public PreguntaResponseDTO actualizar(Integer idPregunta, PreguntaRequestDTO dto) {
        Pregunta pregunta = preguntaRepository.findById(idPregunta)
                .orElseThrow(() -> new NoSuchElementException("Pregunta no encontrada"));

        if (pregunta.getEncuesta().getEstadoEncuesta() != EstadoEncuesta.EN_DISENO) {
            throw new IllegalStateException("No se pueden modificar preguntas de una encuesta publicada");
        }

        pregunta.setDescripcionPregunta(dto.getDescripcionPregunta());
        pregunta.setObligatoriaPregunta(dto.getObligatoriaPregunta());

        return toDTO(preguntaRepository.save(pregunta));
    }

    public void eliminar(Integer idPregunta) {
        Pregunta pregunta = preguntaRepository.findById(idPregunta)
                .orElseThrow(() -> new NoSuchElementException("Pregunta no encontrada"));

        if (pregunta.getEncuesta().getEstadoEncuesta() != EstadoEncuesta.EN_DISENO) {
            throw new IllegalStateException("No se pueden eliminar preguntas de una encuesta publicada");
        }

        preguntaRepository.deleteById(idPregunta);
    }

    private PreguntaResponseDTO toDTO(Pregunta p) {
        PreguntaResponseDTO dto = new PreguntaResponseDTO();
        dto.setIdPregunta(p.getIdPregunta());
        dto.setDescripcionPregunta(p.getDescripcionPregunta());
        dto.setObligatoriaPregunta(p.getObligatoriaPregunta());
        dto.setTipoPregunta(p.getTipoPregunta() != null ? p.getTipoPregunta().name() : null);
        dto.setTipoPreguntaCerrada(p.getTipoPreguntaCerrada() != null ? p.getTipoPreguntaCerrada().name() : null);
        dto.setEsMixta(p.getEsMixta());
        dto.setIdEncuesta(p.getEncuesta().getIdEncuesta());
        return dto;
    }
}
