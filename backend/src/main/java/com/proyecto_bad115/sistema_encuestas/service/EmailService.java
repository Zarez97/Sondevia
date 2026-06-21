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
// los correos se envían vía la API HTTP de Brevo en lugar de JavaMailSender.
// Brevo solo exige verificar la dirección de remitente (no un dominio completo),
// por lo que se puede seguir usando notificaciones.sondevia@gmail.com.
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestClient restClient;

    @Value("${brevo.from}")
    private String remitente;

    public EmailService(@Value("${brevo.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(BREVO_URL)
                .defaultHeader("api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    private void enviar(String destinatario, String asunto, String texto, String replyTo) {
        Map<String, Object> body = new java.util.HashMap<>(Map.of(
                "sender", Map.of("email", remitente, "name", "Sondevia"),
                "to", List.of(Map.of("email", destinatario)),
                "subject", asunto,
                "textContent", texto
        ));
        if (replyTo != null) {
            body.put("replyTo", Map.of("email", replyTo));
        }

        restClient.post()
                .body(body)
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
                "Inicia sesion en el sistema aqui: https://sondevia.up.railway.app/login\n\n" +
                "Equipo Sondevia",
                null);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo de bienvenida a {}: {}", destinatario, e.getMessage());
        }
    }
}
