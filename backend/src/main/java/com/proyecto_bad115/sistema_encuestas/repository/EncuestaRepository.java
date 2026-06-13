package com.proyecto_bad115.sistema_encuestas.repository;

import com.proyecto_bad115.sistema_encuestas.model.Encuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EncuestaRepository extends JpaRepository<Encuesta, Integer> {

    List<Encuesta> findByUsuarioEmailUser(String email);

    List<Encuesta> findByEstadoEncuesta(Integer estado);

    Optional<Encuesta> findByTokenPublico(String tokenPublico);
}
