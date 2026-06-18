package com.proyecto_bad115.sistema_encuestas.repository;

import com.proyecto_bad115.sistema_encuestas.model.DetalleRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleRespuestaRepository extends JpaRepository<DetalleRespuesta, Integer> {

    // CU09 - Detalles de respuesta ENVIADA de una pregunta (para estadísticas; excluye borradores)
    List<DetalleRespuesta> findByPreguntaIdPreguntaAndRespuestaEstadoRespuesta(Integer idPregunta, Integer estado);

    // Etapa 17 - Detalles de una respuesta (reconstruir borrador / limpiar antes de re-guardar)
    List<DetalleRespuesta> findByRespuestaIdRespuesta(Integer idRespuesta);

    void deleteByRespuestaIdRespuesta(Integer idRespuesta);

    // Frecuencia de opciones de una pregunta cerrada (delega a función SQL)
    @Query(value = "SELECT etiqueta, cantidad, porcentaje FROM fn_conteo_opciones_pregunta(:idPregunta)", nativeQuery = true)
    List<Object[]> conteoOpcionesPorPregunta(@Param("idPregunta") Integer idPregunta);

    // Promedio numérico de una pregunta de escala (delega a función SQL)
    @Query(value = "SELECT fn_promedio_escala_pregunta(:idPregunta)", nativeQuery = true)
    Double promedioEscalaPregunta(@Param("idPregunta") Integer idPregunta);
}