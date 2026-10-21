package pe.edu.cibertec.sigip.notificationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.sigip.notificationservice.model.EstadoNotificacion;
import pe.edu.cibertec.sigip.notificationservice.model.Notificacion;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    List<Notificacion> findAllByOrderByFechaDesc();

    List<Notificacion> findByDestinatarioOrderByFechaDesc(String destinatario);

    long countByDestinatarioAndEstadoNot(String destinatario, EstadoNotificacion estado);

    boolean existsByEventId(String eventId);
}
