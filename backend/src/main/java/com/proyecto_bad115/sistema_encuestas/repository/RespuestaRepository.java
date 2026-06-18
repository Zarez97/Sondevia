package com.proyecto_bad115.sistema_encuestas.repository;

import com.proyecto_bad115.sistema_encuestas.model.Respuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Integer> {

    // Control de duplicados: el usuario ya ENVIÓ una respuesta en esta encuesta
    boolean existsByEncuestaIdEncuestaAndUsuarioEmailUserAndEstadoRespuesta(
            Integer idEncuesta, String emailUser, Integer estadoRespuesta);

    // Respuesta del usuario en una encuesta según estado (borrador o enviada)
    Optional<Respuesta> findFirstByEncuestaIdEncuestaAndUsuarioEmailUserAndEstadoRespuesta(
            Integer idEncuesta, String emailUser, Integer estadoRespuesta);

    // CU09 - Total de respuestas ENVIADAS de una encuesta (delega a función SQL)
    @Query(value = "SELECT fn_total_respuestas_encuesta(:idEncuesta)", nativeQuery = true)
    Long totalRespuestasEnviadas(@Param("idEncuesta") Integer idEncuesta);

    // Etapa 18 - Respuestas del encuestado según estado (panel "Mis Encuestas")
    List<Respuesta> findByUsuarioEmailUserAndEstadoRespuesta(String emailUser, Integer estadoRespuesta);

    // Migración: respuestas previas sin estado (se tratan como ENVIADAS)
    List<Respuesta> findByEstadoRespuestaIsNull();

    // Eliminación segura de usuario: ¿tiene respuestas registradas?
    boolean existsByUsuarioIdUser(Integer idUser);
}
