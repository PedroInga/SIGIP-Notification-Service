package pe.edu.cibertec.sigip.notificationservice.model;

/** Ciclo de vida de un aviso: se registra, se envía por el canal y el cliente lo marca como leído. */
public enum EstadoNotificacion {
    PENDIENTE,
    ENVIADA,
    LEIDA
}
