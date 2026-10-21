package pe.edu.cibertec.sigip.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.cibertec.sigip.notificationservice.model.CanalNotificacion;

/** Aviso registrado manualmente por un administrador (por ejemplo, un recordatorio de recojo). */
public record NotificacionRequest(
        @NotBlank @Size(max = 150) String destinatario,
        @NotNull CanalNotificacion canal,
        @NotBlank @Size(max = 200) String asunto,
        @NotBlank @Size(max = 1000) String mensaje,
        Integer idpedido
) {
}
