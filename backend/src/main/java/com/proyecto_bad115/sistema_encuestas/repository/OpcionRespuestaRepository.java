package com.proyecto_bad115.sistema_encuestas.repository;

import com.proyecto_bad115.sistema_encuestas.model.OpcionRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpcionRespuestaRepository extends JpaRepository<OpcionRespuesta, Integer> {

    List<OpcionRespuesta> findByPreguntaIdPreguntaOrderByValorNumericoAsc(Integer idPregunta);

    void deleteByPreguntaIdPregunta(Integer idPregunta);
}
