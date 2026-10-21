package pe.edu.cibertec.sigip.notificationservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones",
        indexes = @Index(name = "idx_notificacion_destinatario", columnList = "destinatario"),
        uniqueConstraints = @UniqueConstraint(name = "uk_notificacion_evento", columnNames = "eventId"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idnotificacion;

    /** Id del evento RabbitMQ que la originó; único para no registrar dos veces el mismo mensaje. */
    @Column(length = 36)
    private String eventId;

    /** Nombre de usuario del cliente que recibe el aviso. */
    @Column(nullable = false, length = 150)
    private String destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalNotificacion canal;

    @Column(nullable = false, length = 200)
    private String asunto;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoNotificacion estado;

    private Integer idpedido;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private LocalDateTime fechaLectura;
}
