package com.proyecto_bad115.sistema_encuestas.repository;

import com.proyecto_bad115.sistema_encuestas.model.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmailUser(String emailUser);

    boolean existsByEmailUser(String emailUser);

    // Procedure: resetea intentos fallidos y activa el usuario
    @Transactional
    @Modifying
    @Query(value = "CALL sp_desbloquear_usuario(:id)", nativeQuery = true)
    void callDesbloquearUsuario(@Param("id") Integer id);

    // Vista: usuarios con roles concatenados y datos completos
    @Query(value = "SELECT * FROM v_usuarios_roles", nativeQuery = true)
    List<Object[]> findUsuariosConRoles();
}