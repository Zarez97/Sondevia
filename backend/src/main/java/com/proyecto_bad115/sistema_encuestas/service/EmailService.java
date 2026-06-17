package com.proyecto_bad115.sistema_encuestas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

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
