package com.proyecto_bad115.sistema_encuestas.repository;

import com.proyecto_bad115.sistema_encuestas.model.DetalleRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleRespuestaRepository extends JpaRepository<DetalleRespuesta, Integer> {

    // CU09 - Detalles de respuesta ENVIADA de una pregunta (para estadísticas; excluye borradores)
    List<DetalleRespuesta> findByPreguntaIdPreguntaAndRespuestaEstadoRespuesta(Integer idPregunta, Integer estado);

    // Etapa 17 - Detalles de una respuesta (reconstruir borrador / limpiar antes de re-guardar)
    List<DetalleRespuesta> findByRespuestaIdRespuesta(Integer idRespuesta);

    void deleteByRespuestaIdRespuesta(Integer idRespuesta);
}