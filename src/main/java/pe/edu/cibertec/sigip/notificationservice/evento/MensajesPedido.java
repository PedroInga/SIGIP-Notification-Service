package pe.edu.cibertec.sigip.notificationservice.evento;

import pe.edu.cibertec.sigip.common.evento.PedidoEvento;

/** Traduce el estado de un pedido al texto que verá el cliente. */
public final class MensajesPedido {

    private MensajesPedido() {
    }

    public static String asunto(PedidoEvento e) {
        return "Pedido #" + e.idpedido() + ": " + switch (e.estado()) {
            case "REGISTRADO" -> "registrado";
            case "PAGO_EN_REVISION" -> "pago en revisión";
            case "CONFIRMADO" -> "pago confirmado";
            case "EN_DISENO" -> "en diseño";
            case "EN_PRODUCCION" -> "en producción";
            case "TERMINADO" -> "listo para recoger";
            case "ENTREGADO" -> "entregado";
            case "CANCELADO" -> "cancelado";
            default -> e.estado();
        };
    }

    public static String mensaje(PedidoEvento e) {
        String detalle = switch (e.estado()) {
            case "REGISTRADO" -> "Recibimos tu pedido. Registra el comprobante de pago para continuar.";
            case "PAGO_EN_REVISION" -> "Estamos verificando tu pago.";
            case "CONFIRMADO" -> "Tu pago fue verificado y el pedido entra a la cola de trabajo.";
            case "EN_DISENO" -> "Nuestro equipo está preparando el diseño; te pediremos aprobarlo.";
            case "EN_PRODUCCION" -> "Tu pedido se está imprimiendo.";
            case "TERMINADO" -> "Tu pedido pasó el control de calidad y está listo para recoger.";
            case "ENTREGADO" -> "Gracias por tu compra.";
            case "CANCELADO" -> "Tu pedido fue cancelado. Si tienes dudas, contáctanos.";
            default -> "El estado de tu pedido cambió a " + e.estado() + ".";
        };
        return "Hola " + e.cliente() + ", " + detalle + (e.total() != null ? " Total: S/ " + e.total() + "." : "");
    }
}
