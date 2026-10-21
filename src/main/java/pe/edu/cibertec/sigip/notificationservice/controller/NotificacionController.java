package pe.edu.cibertec.sigip.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.sigip.common.security.UsuarioActual;
import pe.edu.cibertec.sigip.notificationservice.dto.NotificacionRequest;
import pe.edu.cibertec.sigip.notificationservice.dto.NotificacionResponse;
import pe.edu.cibertec.sigip.notificationservice.service.NotificacionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService service;

    /** ADMIN ve todas; un cliente solo las suyas. */
    @GetMapping
    public List<NotificacionResponse> listar(Authentication auth) {
        return service.listar(UsuarioActual.de(auth));
    }

    @GetMapping("/no-leidas")
    public Map<String, Long> noLeidas(Authentication auth) {
        return Map.of("noLeidas", service.contarNoLeidas(UsuarioActual.de(auth)));
    }

    @GetMapping("/{id}")
    public NotificacionResponse obtener(@PathVariable Integer id, Authentication auth) {
        return service.obtener(id, UsuarioActual.de(auth));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificacionResponse> crear(@Valid @RequestBody NotificacionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public NotificacionResponse actualizar(@PathVariable Integer id, @Valid @RequestBody NotificacionRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/leida")
    public NotificacionResponse marcarLeida(@PathVariable Integer id, Authentication auth) {
        return service.marcarLeida(id, UsuarioActual.de(auth));
    }
}
