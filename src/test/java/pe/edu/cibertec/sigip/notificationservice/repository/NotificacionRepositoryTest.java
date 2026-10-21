package pe.edu.cibertec.sigip.notificationservice.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.cibertec.sigip.notificationservice.model.CanalNotificacion;
import pe.edu.cibertec.sigip.notificationservice.model.EstadoNotificacion;
import pe.edu.cibertec.sigip.notificationservice.model.Notificacion;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas de la capa de datos: insertar, listar, actualizar y eliminar notificaciones. */
@DataJpaTest
@ActiveProfiles("test")
class NotificacionRepositoryTest {

    @Autowired
    private NotificacionRepository repo;

    private Notificacion nueva(String destinatario, String eventId) {
        return Notificacion.builder().eventId(eventId).destinatario(destinatario).canal(CanalNotificacion.SISTEMA)
                .asunto("Pedido #1").mensaje("Tu pedido cambió de estado").estado(EstadoNotificacion.ENVIADA)
                .idpedido(1).fecha(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("Inserta y lista por destinatario")
    void insertarYListar() {
        repo.save(nueva("ana", "e-1"));
        repo.save(nueva("ana", "e-2"));
        repo.save(nueva("luis", "e-3"));

        assertThat(repo.findByDestinatarioOrderByFechaDesc("ana")).hasSize(2);
        assertThat(repo.findAllByOrderByFechaDesc()).hasSize(3);
        assertThat(repo.existsByEventId("e-3")).isTrue();
    }

    @Test
    @DisplayName("Actualiza el estado a LEIDA y cuenta las no leídas")
    void actualizar() {
        Notificacion n = repo.save(nueva("ana", "e-1"));
        repo.save(nueva("ana", "e-2"));
        n.setEstado(EstadoNotificacion.LEIDA);
        repo.saveAndFlush(n);

        assertThat(repo.countByDestinatarioAndEstadoNot("ana", EstadoNotificacion.LEIDA)).isEqualTo(1);
    }

    @Test
    @DisplayName("Elimina una notificación")
    void eliminar() {
        Notificacion n = repo.save(nueva("ana", "e-1"));
        repo.delete(n);
        assertThat(repo.findById(n.getIdnotificacion())).isEmpty();
    }

    @Test
    @DisplayName("El eventId es único: la base de datos rechaza un evento duplicado")
    void eventoDuplicado() {
        repo.saveAndFlush(nueva("ana", "e-1"));
        assertThatThrownBy(() -> repo.saveAndFlush(nueva("ana", "e-1")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
