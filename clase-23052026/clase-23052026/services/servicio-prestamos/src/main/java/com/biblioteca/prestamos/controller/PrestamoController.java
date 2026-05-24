package com.biblioteca.prestamos.controller;

import com.biblioteca.prestamos.model.Prestamo;
import com.biblioteca.prestamos.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/prestamos")
@Tag(name = "Préstamos", description = "Task Service — Orquestación de préstamos de libros")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @PostMapping
    @Operation(
        summary = "Crear un préstamo",
        description = """
            COMPOSICIÓN DE SERVICIOS: internamente este endpoint:
            1. Llama a GET /usuarios/{id} (Servicio de Usuarios)
            2. Llama a GET /libros/{id} (Servicio de Libros)
            3. Llama a PATCH /libros/{id}/disponibilidad (Servicio de Libros)
            4. Registra el préstamo localmente
            Todo de forma SÍNCRONA: espera cada respuesta antes de continuar.
            """
    )
    public ResponseEntity<?> crearPrestamo(@RequestBody Map<String, String> solicitud) {
        String usuarioId = solicitud.get("usuario_id");
        String libroId   = solicitud.get("libro_id");

        if (usuarioId == null || libroId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Se requieren los campos 'usuario_id' y 'libro_id'"));
        }

        try {
            Prestamo prestamo = prestamoService.crearPrestamo(usuarioId, libroId);
            return ResponseEntity.status(HttpStatus.CREATED).body(prestamo);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Listar todos los préstamos")
    public List<Prestamo> listarTodos() {
        return prestamoService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener préstamo por ID")
    public ResponseEntity<Prestamo> obtenerPorId(@PathVariable String id) {
        return prestamoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/devolver")
    @Operation(
        summary = "Registrar devolución",
        description = "Llama al Servicio de Libros para marcar el libro como disponible nuevamente."
    )
    public ResponseEntity<?> devolver(@PathVariable String id) {
        try {
            return ResponseEntity.ok(prestamoService.devolverLibro(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "Estado del servicio y sus dependencias",
        description = "BUENA PRÁCTICA: un health check real verifica que los servicios dependientes están disponibles."
    )
    public Map<String, Object> health() {
        Map<String, String> dependencias = prestamoService.verificarDependencias();
        return Map.of(
            "servicio",      "prestamos",
            "estado",        "activo",
            "version",       "1.0.0",
            "dependencias",  dependencias
        );
    }
}
