package pe.edu.cibertec.sigip.notificationservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.cibertec.sigip.common.evento.PedidoEvento;
import pe.edu.cibertec.sigip.common.security.JwtService;
import pe.edu.cibertec.sigip.notificationservice.repository.NotificacionRepository;
import pe.edu.cibertec.sigip.notificationservice.service.NotificacionService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Consumo de eventos (idempotente) y endpoints REST de notificaciones. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificacionIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private NotificacionRepository repo;
    @Autowired
    private NotificacionService service;

    private String admin;
    private String ana;
    private String luis;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        admin = "Bearer " + jwtService.generarToken("admin", List.of("ROLE_ADMIN"));
        ana = "Bearer " + jwtService.generarToken("ana", List.of("ROLE_CLIENTE"));
        luis = "Bearer " + jwtService.generarToken("luis", List.of("ROLE_CLIENTE"));
    }

    @Test
    @DisplayName("Un mismo evento entregado dos veces genera una sola notificación")
    void eventoIdempotente() throws Exception {
        PedidoEvento evento = PedidoEvento.de("PEDIDO_ESTADO", 7, "ana", "TERMINADO", new BigDecimal("45.00"));

        assertThat(service.registrarDesdeEvento(evento)).isPresent();
        assertThat(service.registrarDesdeEvento(evento)).isEmpty();

        mvc.perform(get("/api/v1/notificaciones").header("Authorization", ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].asunto").value("Pedido #7: listo para recoger"));
    }

    @Test
    @DisplayName("El cliente solo ve y marca sus avisos; los ajenos responden 404")
    void propiedad() throws Exception {
        var n = service.registrarDesdeEvento(PedidoEvento.de("PEDIDO_ESTADO", 3, "ana", "CONFIRMADO", null))
                .orElseThrow();
        String url = "/api/v1/notificaciones/" + n.getIdnotificacion();

        mvc.perform(get(url).header("Authorization", luis)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/notificaciones/no-leidas").header("Authorization", ana))
                .andExpect(jsonPath("$.noLeidas").value(1));

        mvc.perform(patch(url + "/leida").header("Authorization", ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("LEIDA"));
        mvc.perform(get("/api/v1/notificaciones/no-leidas").header("Authorization", ana))
                .andExpect(jsonPath("$.noLeidas").value(0));
    }

    @Test
    @DisplayName("CRUD de ADMIN; un cliente no puede crear (403) y sin token es 401")
    void crudAdmin() throws Exception {
        String body = """
                {"destinatario":"ana","canal":"EMAIL","asunto":"Recordatorio","mensaje":"Tu pedido te espera","idpedido":3}""";

        mvc.perform(post("/api/v1/notificaciones").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/notificaciones").header("Authorization", ana)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/notificaciones").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        Integer id = repo.findAll().getFirst().getIdnotificacion();
        mvc.perform(put("/api/v1/notificaciones/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("Recordatorio", "Último aviso")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asunto").value("Último aviso"));
        mvc.perform(post("/api/v1/notificaciones").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"destinatario\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/v1/notificaciones/" + id).header("Authorization", admin))
                .andExpect(status().isNoContent());
        assertThat(repo.count()).isZero();
    }
}
