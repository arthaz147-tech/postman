package com.biblioteca.libros.controller;

import com.biblioteca.libros.model.Libro;
import com.biblioteca.libros.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/libros")
@Tag(name = "Libros", description = "Entity Service — Gestión del catálogo de libros")
public class LibroController {

    private final LibroService libroService;

    // Inyección de dependencias (principio de inversión de dependencia)
    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    // ── CONTRATO DEL SERVICIO (endpoints públicos) ────────────────

    @GetMapping
    @Operation(summary = "Listar todos los libros",
               description = "STATELESS: cada request es independiente. Retorna el catálogo completo.")
    public List<Libro> listarTodos() {
        return libroService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener libro por ID",
               description = "REMOTO: el cliente solo necesita la URL y el ID para acceder al recurso.")
    public ResponseEntity<Libro> obtenerPorId(@PathVariable String id) {
        return libroService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo libro",
               description = "ENCAPSULACIÓN: el servidor genera el ID internamente. El cliente solo provee datos de negocio.")
    public Libro crear(@RequestBody Libro libro) {
        return libroService.crear(libro);
    }

    @PatchMapping("/{id}/disponibilidad")
    @Operation(summary = "Cambiar disponibilidad de un libro",
               description = "Usado por el Servicio de Préstamos para marcar libros como prestados/disponibles. INTEROPERABILIDAD entre servicios.")
    public ResponseEntity<Libro> cambiarDisponibilidad(
            @PathVariable String id,
            @RequestParam boolean disponible) {
        return libroService.cambiarDisponibilidad(id, disponible)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un libro")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable String id) {
        if (libroService.eliminar(id)) {
            return ResponseEntity.ok(Map.of("mensaje", "Libro '" + id + "' eliminado correctamente"));
        }
        return ResponseEntity.notFound().build();
    }

    // ── Health check manual (además del /actuator/health de Spring) ─
    @GetMapping("/health")
    @Operation(summary = "Estado del servicio")
    public Map<String, String> health() {
        return Map.of(
            "servicio", "libros",
            "estado",   "activo",
            "version",  "1.0.0"
        );
    }
}
