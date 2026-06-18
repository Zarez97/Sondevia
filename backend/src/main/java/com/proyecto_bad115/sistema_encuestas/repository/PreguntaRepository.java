package com.proyecto_bad115.sistema_encuestas.repository;

import com.proyecto_bad115.sistema_encuestas.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {

    List<Pregunta> findByEncuestaIdEncuesta(Integer idEncuesta);

    int countByEncuestaIdEncuesta(Integer idEncuesta);
}
