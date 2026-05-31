package com.proyecto_bad115.sistema_encuestas.config;

import com.proyecto_bad115.sistema_encuestas.model.Usuario;
import com.proyecto_bad115.sistema_encuestas.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByEmailUser("admin@encuestas.com")) {
            Usuario admin = new Usuario();
            admin.setNombreUser("Administrador");
            admin.setEmailUser("admin@encuestas.com");
            admin.setContraseniaUser(passwordEncoder.encode("admin123"));
            admin.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            admin.setEstadoUser(1);
            admin.setIntentosFallidos(0);
            usuarioRepository.save(admin);
            System.out.println(">>> Usuario administrador creado: admin@encuestas.com / admin123");
        }
    }
}
