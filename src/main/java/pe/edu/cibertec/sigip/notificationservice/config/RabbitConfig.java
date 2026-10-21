package pe.edu.cibertec.sigip.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.cibertec.sigip.common.evento.PedidoEvento;

/**
 * Topología RabbitMQ: exchange de pedidos → cola de notificaciones, con una cola de fallos (DLQ)
 * para los mensajes que no se pudieron procesar tras los reintentos.
 */
@Configuration
public class RabbitConfig {

    public static final String DLX = "sigip.pedidos.dlx";

    @Value("${sigip.rabbit.queue:sigip.notificaciones}")
    private String cola;

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(PedidoEvento.EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(cola)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(cola + ".dlq")
                .build();
    }

    @Bean
    public Queue notificacionesDlq() {
        return QueueBuilder.durable(cola + ".dlq").build();
    }

    @Bean
    public Binding bindingPedidos(Queue notificacionesQueue, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(notificacionesQueue).to(pedidosExchange).with("pedido.*");
    }

    @Bean
    public Binding bindingDlq(Queue notificacionesDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(notificacionesDlq).to(deadLetterExchange).with(cola + ".dlq");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
