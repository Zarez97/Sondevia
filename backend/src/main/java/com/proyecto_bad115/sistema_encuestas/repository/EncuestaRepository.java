package com.proyecto_bad115.sistema_encuestas.repository;

import com.proyecto_bad115.sistema_encuestas.model.Encuesta;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EncuestaRepository extends JpaRepository<Encuesta, Integer> {

    List<Encuesta> findByUsuarioEmailUser(String email);

    List<Encuesta> findByEstadoEncuesta(Integer estado);

    Optional<Encuesta> findByTokenPublico(String tokenPublico);

    // Eliminación segura de usuario: ¿es dueño de alguna encuesta?
    boolean existsByUsuarioIdUser(Integer idUser);

    // Procedure: valida requisitos, asigna token y publica la encuesta
    @Transactional
    @Modifying
    @Query(value = "CALL sp_publicar_encuesta(:id, :token)", nativeQuery = true)
    void callPublicarEncuesta(@Param("id") Integer id, @Param("token") String token);

    // Procedure: valida que esté publicada y cambia el estado a CERRADA
    @Transactional
    @Modifying
    @Query(value = "CALL sp_cerrar_encuesta(:id)", nativeQuery = true)
    void callCerrarEncuesta(@Param("id") Integer id);

    // Vista: encuestas publicadas y vigentes (catálogo público)
    @Query(value = "SELECT * FROM v_encuestas_disponibles", nativeQuery = true)
    List<Object[]> findEncuestasDisponiblesView();

    // Vista: todas las encuestas con totales de preguntas y respuestas
    @Query(value = "SELECT * FROM v_resumen_encuestas", nativeQuery = true)
    List<Object[]> findResumenTodasEncuestas();

    // Vista: encuestas de un usuario con totales
    @Query(value = "SELECT * FROM v_resumen_encuestas WHERE email_usuario = :email", nativeQuery = true)
    List<Object[]> findResumenEncuestasByUsuario(@Param("email") String email);
}
