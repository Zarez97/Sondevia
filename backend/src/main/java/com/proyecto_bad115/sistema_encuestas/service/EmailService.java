package com.proyecto_bad115.sistema_encuestas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JavaMailSender mailSender;

    // El remitente siempre coincide con la cuenta autenticada en application.yaml
    @Value("${spring.mail.username}")
    private String remitente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarDesbloqueo(String destinatario, String nombreUsuario) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject("Sondevia - Cuenta desbloqueada");
            mensaje.setText(
                "Hola " + nombreUsuario + ",\n\n" +
                "Tu cuenta en Sondevia ha sido desbloqueada por el administrador del sistema.\n" +
                "Ya puedes iniciar sesion con tus credenciales.\n\n" +
                "Si no solicitaste este desbloqueo, contacta al administrador.\n\n" +
                "Equipo Sondevia"
            );
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo de desbloqueo a {}: {}", destinatario, e.getMessage());
        }
    }

    /** Notifica al administrador que un usuario bloqueado solicita el desbloqueo de su cuenta. */
    @Async
    public void enviarSolicitudDesbloqueo(String emailUsuario, String nombreUsuario) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(remitente); // buzón del administrador (notificaciones.sondevia@gmail.com)
            mensaje.setReplyTo(emailUsuario);
            mensaje.setSubject("Sondevia - Solicitud de desbloqueo de cuenta");
            mensaje.setText(
                "Hola administrador,\n\n" +
                "Un usuario ha solicitado el desbloqueo de su cuenta en Sondevia:\n\n" +
                "  Nombre: " + nombreUsuario + "\n" +
                "  Correo: " + emailUsuario + "\n" +
                "  Fecha de solicitud: " + LocalDateTime.now().format(FECHA_HORA) + "\n\n" +
                "La cuenta fue bloqueada por multiples intentos fallidos de inicio de sesion.\n" +
                "Verifica la identidad del usuario y desbloquea la cuenta desde el panel de\n" +
                "administracion (Usuarios bloqueados) si corresponde.\n\n" +
                "Equipo Sondevia"
            );
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.warn("No se pudo enviar la solicitud de desbloqueo de {}: {}", emailUsuario, e.getMessage());
        }
    }

    @Async
    public void enviarBienvenida(String destinatario, String nombreUsuario, String contraseniaTemporal) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject("Sondevia - Bienvenido al sistema");
            mensaje.setText(
                "Hola " + nombreUsuario + ",\n\n" +
                "Tu cuenta en Sondevia ha sido creada exitosamente.\n" +
                "Tus credenciales de acceso son:\n" +
                "  Correo: " + destinatario + "\n" +
                "  Contraseña temporal: " + contraseniaTemporal + "\n\n" +
                "Por favor cambia tu contraseña al iniciar sesion por primera vez.\n\n" +
                "Equipo Sondevia"
            );
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo de bienvenida a {}: {}", destinatario, e.getMessage());
        }
    }
}
