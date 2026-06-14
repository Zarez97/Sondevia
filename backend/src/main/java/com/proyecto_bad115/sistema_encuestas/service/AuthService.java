package com.proyecto_bad115.sistema_encuestas.service;

import com.proyecto_bad115.sistema_encuestas.config.PasswordPolicyValidator;
import com.proyecto_bad115.sistema_encuestas.dto.LoginRequestDTO;
import com.proyecto_bad115.sistema_encuestas.dto.LoginResponseDTO;
import com.proyecto_bad115.sistema_encuestas.dto.MenuItemDTO;
import com.proyecto_bad115.sistema_encuestas.dto.RegistroRequestDTO;
import com.proyecto_bad115.sistema_encuestas.model.EstadoUsuario;
import com.proyecto_bad115.sistema_encuestas.model.Usuario;
import com.proyecto_bad115.sistema_encuestas.model.UsuarioRol;
import com.proyecto_bad115.sistema_encuestas.repository.RolPrivilegioRepository;
import com.proyecto_bad115.sistema_encuestas.repository.RolRepository;
import com.proyecto_bad115.sistema_encuestas.repository.UsuarioRepository;
import com.proyecto_bad115.sistema_encuestas.repository.UsuarioRolRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AuthService {

    private static final int MAX_INTENTOS = 3;

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolPrivilegioRepository rolPrivilegioRepository;
    private final RolRepository rolRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository,
                       UsuarioRolRepository usuarioRolRepository,
                       RolPrivilegioRepository rolPrivilegioRepository,
                       RolRepository rolRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.rolPrivilegioRepository = rolPrivilegioRepository;
        this.rolRepository = rolRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * CU14 - Auto-registro de un encuestado. Crea la cuenta con rol ENCUESTADO,
     * contraseña real (BCrypt) y devuelve el token para iniciar sesión automáticamente.
     */
    @Transactional
    public LoginResponseDTO registrar(RegistroRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmailUser(email)) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }
        PasswordPolicyValidator.validate(dto.getContrasenia());
        if (dto.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreUser(dto.getNombre().trim());
        usuario.setEmailUser(email);
        usuario.setContraseniaUser(passwordEncoder.encode(dto.getContrasenia()));
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setEstadoUser(EstadoUsuario.ACTIVO);
        usuario.setIntentosFallidos(0);
        Usuario guardado = usuarioRepository.save(usuario);

        rolRepository.findByNombreRol("ENCUESTADO").ifPresent(rol -> {
            UsuarioRol ur = new UsuarioRol();
            ur.setUsuario(guardado);
            ur.setRol(rol);
            usuarioRolRepository.save(ur);
        });

        List<String> roles = usuarioRolRepository.findByUsuario(guardado)
                .stream().map(ur -> ur.getRol().getNombreRol()).toList();

        String token = jwtService.generateToken(guardado.getEmailUser());
        return new LoginResponseDTO(token, guardado.getNombreUser(), guardado.getEmailUser(), roles);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmailUser(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (usuario.getEstadoUser() == EstadoUsuario.BLOQUEADO) {
            throw new LockedException("Cuenta bloqueada por multiples intentos fallidos. Contacte al administrador.");
        }

        if (usuario.getEstadoUser() == EstadoUsuario.INACTIVO) {
            throw new DisabledException("Cuenta inactiva. Contacte al administrador.");
        }

        if (!passwordEncoder.matches(request.getContrasenia(), usuario.getContraseniaUser())) {
            int intentos = usuario.getIntentosFallidos() + 1;
            usuario.setIntentosFallidos(intentos);

            if (intentos >= MAX_INTENTOS) {
                usuario.setEstadoUser(EstadoUsuario.BLOQUEADO);
                usuarioRepository.save(usuario);
                throw new LockedException("Cuenta bloqueada por " + MAX_INTENTOS + " intentos fallidos. Contacte al administrador.");
            }

            usuarioRepository.save(usuario);
            int restantes = MAX_INTENTOS - intentos;
            throw new BadCredentialsException("Credenciales invalidas. Intentos restantes: " + restantes);
        }

        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);

        List<String> roles = usuarioRolRepository.findByUsuario(usuario)
                .stream()
                .map(ur -> ur.getRol().getNombreRol())
                .toList();

        String token = jwtService.generateToken(usuario.getEmailUser());
        return new LoginResponseDTO(token, usuario.getNombreUser(), usuario.getEmailUser(), roles);
    }

    public List<MenuItemDTO> obtenerMenu(String email) {
        return rolPrivilegioRepository.findPrivilegiosByUsuarioEmail(email)
                .stream()
                .map(p -> new MenuItemDTO(p.getNombrePrivilegio(), p.getUrlPrivilegio()))
                .toList();
    }
}
