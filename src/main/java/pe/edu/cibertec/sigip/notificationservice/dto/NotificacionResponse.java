package pe.edu.cibertec.sigip.notificationservice.dto;

import pe.edu.cibertec.sigip.notificationservice.model.CanalNotificacion;
import pe.edu.cibertec.sigip.notificationservice.model.EstadoNotificacion;
import pe.edu.cibertec.sigip.notificationservice.model.Notificacion;

import java.time.LocalDateTime;

public record NotificacionResponse(
        Integer idnotificacion,
        String destinatario,
        CanalNotificacion canal,
        String asunto,
        String mensaje,
        EstadoNotificacion estado,
        Integer idpedido,
        LocalDateTime fecha,
        LocalDateTime fechaLectura
) {
    public static NotificacionResponse de(Notificacion n) {
        return new NotificacionResponse(n.getIdnotificacion(), n.getDestinatario(), n.getCanal(), n.getAsunto(),
                n.getMensaje(), n.getEstado(), n.getIdpedido(), n.getFecha(), n.getFechaLectura());
    }
}
