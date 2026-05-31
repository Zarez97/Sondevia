package com.proyecto_bad115.sistema_encuestas.repository;

import com.proyecto_bad115.sistema_encuestas.model.Privilegio;
import com.proyecto_bad115.sistema_encuestas.model.RolPrivilegio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolPrivilegioRepository extends JpaRepository<RolPrivilegio, Integer> {

    List<RolPrivilegio> findByRolIdRol(Integer idRol);

    @Query("SELECT DISTINCT rp.privilegio FROM RolPrivilegio rp WHERE rp.rol IN " +
           "(SELECT ur.rol FROM UsuarioRol ur WHERE ur.usuario.emailUser = :email)")
    List<Privilegio> findPrivilegiosByUsuarioEmail(@Param("email") String email);
}
