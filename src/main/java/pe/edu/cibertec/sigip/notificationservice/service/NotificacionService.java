package pe.edu.cibertec.sigip.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.sigip.common.error.RecursoNoEncontradoException;
import pe.edu.cibertec.sigip.common.evento.PedidoEvento;
import pe.edu.cibertec.sigip.common.security.UsuarioActual;
import pe.edu.cibertec.sigip.notificationservice.dto.NotificacionRequest;
import pe.edu.cibertec.sigip.notificationservice.dto.NotificacionResponse;
import pe.edu.cibertec.sigip.notificationservice.evento.MensajesPedido;
import pe.edu.cibertec.sigip.notificationservice.model.CanalNotificacion;
import pe.edu.cibertec.sigip.notificationservice.model.EstadoNotificacion;
import pe.edu.cibertec.sigip.notificationservice.model.Notificacion;
import pe.edu.cibertec.sigip.notificationservice.repository.NotificacionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository repo;

    /**
     * Registra el aviso de un evento de pedido. Es idempotente: RabbitMQ puede entregar el mismo
     * mensaje más de una vez (reintentos, reconexiones) y solo debe quedar una notificación.
     */
    @Transactional
    public Optional<Notificacion> registrarDesdeEvento(PedidoEvento evento) {
        if (evento.eventId() != null && repo.existsByEventId(evento.eventId())) {
            log.info("Evento {} ya procesado, se descarta", evento.eventId());
            return Optional.empty();
        }
        Notificacion n = Notificacion.builder()
                .eventId(evento.eventId())
                .destinatario(evento.cliente())
                .canal(CanalNotificacion.SISTEMA)
                .asunto(MensajesPedido.asunto(evento))
                .mensaje(MensajesPedido.mensaje(evento))
                .estado(EstadoNotificacion.ENVIADA)
                .idpedido(evento.idpedido())
                .fecha(LocalDateTime.now())
                .build();
        return Optional.of(repo.save(n));
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listar(UsuarioActual usuario) {
        var lista = usuario.esAdmin()
                ? repo.findAllByOrderByFechaDesc()
                : repo.findByDestinatarioOrderByFechaDesc(usuario.nombre());
        return lista.stream().map(NotificacionResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(UsuarioActual usuario) {
        return repo.countByDestinatarioAndEstadoNot(usuario.nombre(), EstadoNotificacion.LEIDA);
    }

    @Transactional(readOnly = true)
    public NotificacionResponse obtener(Integer id, UsuarioActual usuario) {
        return NotificacionResponse.de(buscarVisible(id, usuario));
    }

    @Transactional
    public NotificacionResponse crear(NotificacionRequest req) {
        Notificacion n = Notificacion.builder()
                .estado(EstadoNotificacion.PENDIENTE)
                .fecha(LocalDateTime.now())
                .build();
        copiar(req, n);
        return NotificacionResponse.de(repo.save(n));
    }

    @Transactional
    public NotificacionResponse actualizar(Integer id, NotificacionRequest req) {
        Notificacion n = buscar(id);
        copiar(req, n);
        return NotificacionResponse.de(n);
    }

    @Transactional
    public void eliminar(Integer id) {
        repo.delete(buscar(id));
    }

    @Transactional
    public NotificacionResponse marcarLeida(Integer id, UsuarioActual usuario) {
        Notificacion n = buscarVisible(id, usuario);
        if (n.getEstado() != EstadoNotificacion.LEIDA) {
            n.setEstado(EstadoNotificacion.LEIDA);
            n.setFechaLectura(LocalDateTime.now());
        }
        return NotificacionResponse.de(n);
    }

    private void copiar(NotificacionRequest req, Notificacion n) {
        n.setDestinatario(req.destinatario());
        n.setCanal(req.canal());
        n.setAsunto(req.asunto());
        n.setMensaje(req.mensaje());
        n.setIdpedido(req.idpedido());
    }

    private Notificacion buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación " + id + " no encontrada"));
    }

    /** Un cliente solo ve sus propios avisos; los ajenos se reportan como inexistentes (404). */
    private Notificacion buscarVisible(Integer id, UsuarioActual usuario) {
        Notificacion n = buscar(id);
        if (!usuario.esAdmin() && !n.getDestinatario().equals(usuario.nombre())) {
            throw new RecursoNoEncontradoException("Notificación " + id + " no encontrada");
        }
        return n;
    }
}
