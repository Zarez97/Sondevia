package com.proyecto_bad115.sistema_encuestas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

// Railway bloquea las conexiones salientes SMTP (puertos 587/465), por lo que
// los correos se envían vía la API HTTP de Resend en lugar de JavaMailSender.
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String RESEND_URL = "https://api.resend.com/emails";

    private final RestClient restClient;

    @Value("${resend.from}")
    private String remitente;

    public EmailService(@Value("${resend.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(RESEND_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    private void enviar(String destinatario, String asunto, String texto, String replyTo) {
        restClient.post()
                .body(Map.of(
                        "from", remitente,
                        "to", List.of(destinatario),
                        "subject", asunto,
                        "text", texto,
                        "reply_to", replyTo == null ? List.of() : List.of(replyTo)
                ))
                .retrieve()
                .toBodilessEntity();
    }

    @Async
    public void enviarDesbloqueo(String destinatario, String nombreUsuario) {
        try {
            enviar(destinatario, "Sondevia - Cuenta desbloqueada",
                "Hola " + nombreUsuario + ",\n\n" +
                "Tu cuenta en Sondevia ha sido desbloqueada por el administrador del sistema.\n" +
                "Ya puedes iniciar sesion con tus credenciales.\n\n" +
                "Si no solicitaste este desbloqueo, contacta al administrador.\n\n" +
                "Equipo Sondevia",
                null);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo de desbloqueo a {}: {}", destinatario, e.getMessage());
        }
    }

    /** Notifica al administrador que un usuario bloqueado solicita el desbloqueo de su cuenta. */
    @Async
    public void enviarSolicitudDesbloqueo(String emailUsuario, String nombreUsuario) {
        try {
            enviar(remitente, "Sondevia - Solicitud de desbloqueo de cuenta",
                "Hola administrador,\n\n" +
                "Un usuario ha solicitado el desbloqueo de su cuenta en Sondevia:\n\n" +
                "  Nombre: " + nombreUsuario + "\n" +
                "  Correo: " + emailUsuario + "\n" +
                "  Fecha de solicitud: " + LocalDateTime.now().format(FECHA_HORA) + "\n\n" +
                "La cuenta fue bloqueada por multiples intentos fallidos de inicio de sesion.\n" +
                "Verifica la identidad del usuario y desbloquea la cuenta desde el panel de\n" +
                "administracion (Usuarios bloqueados) si corresponde.\n\n" +
                "Equipo Sondevia",
                emailUsuario);
        } catch (Exception e) {
            log.warn("No se pudo enviar la solicitud de desbloqueo de {}: {}", emailUsuario, e.getMessage());
        }
    }

    @Async
    public void enviarBienvenida(String destinatario, String nombreUsuario, String contraseniaTemporal) {
        try {
            enviar(destinatario, "Sondevia - Bienvenido al sistema",
                "Hola " + nombreUsuario + ",\n\n" +
                "Tu cuenta en Sondevia ha sido creada exitosamente.\n" +
                "Tus credenciales de acceso son:\n" +
                "  Correo: " + destinatario + "\n" +
                "  Contraseña temporal: " + contraseniaTemporal + "\n\n" +
                "Por favor cambia tu contraseña al iniciar sesion por primera vez.\n\n" +
                "Equipo Sondevia",
                null);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo de bienvenida a {}: {}", destinatario, e.getMessage());
        }
    }
}
