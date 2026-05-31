package com.proyecto_bad115.sistema_encuestas.service;

import com.proyecto_bad115.sistema_encuestas.dto.LoginRequestDTO;
import com.proyecto_bad115.sistema_encuestas.dto.LoginResponseDTO;
import com.proyecto_bad115.sistema_encuestas.model.Usuario;
import com.proyecto_bad115.sistema_encuestas.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmailUser(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.getContrasenia(), usuario.getContraseniaUser())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        String token = jwtService.generateToken(usuario.getEmailUser());
        return new LoginResponseDTO(token, usuario.getNombreUser(), usuario.getEmailUser());
    }
}
