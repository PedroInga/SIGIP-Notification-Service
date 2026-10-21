package pe.edu.cibertec.sigip.notificationservice.evento;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.edu.cibertec.sigip.common.evento.PedidoEvento;
import pe.edu.cibertec.sigip.notificationservice.service.NotificacionService;

/**
 * Consumidor de los eventos de pedidos. Si el procesamiento falla, Spring reintenta
 * (spring.rabbitmq.listener.simple.retry) y al agotarse el mensaje pasa a la cola de fallos (DLQ).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sigip.eventos.habilitados", havingValue = "true", matchIfMissing = true)
public class PedidoEventoListener {

    private final NotificacionService service;

    @RabbitListener(queues = "${sigip.rabbit.queue:sigip.notificaciones}")
    public void recibir(PedidoEvento evento) {
        log.info("Evento recibido: {} pedido {} -> {}", evento.tipo(), evento.idpedido(), evento.estado());
        service.registrarDesdeEvento(evento);
    }
}
