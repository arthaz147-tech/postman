package com.biblioteca.usuarios.controller;

import com.biblioteca.usuarios.model.Usuario;
import com.biblioteca.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Entity Service — Gestión de usuarios de la biblioteca")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios")
    public List<Usuario> listarTodos() {
        return usuarioService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID",
               description = "El Servicio de Préstamos llama a este endpoint para validar el usuario antes de crear un préstamo. Esto es INTEROPERABILIDAD entre servicios SOA.")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable String id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nuevo usuario",
               description = "BUENA PRÁCTICA: valida DNI único. La lógica de negocio vive en el servicio, no en el cliente.")
    public ResponseEntity<?> crear(@RequestBody Usuario usuario) {
        // Validación de regla de negocio: DNI único
        if (usuarioService.buscarIdPorDni(usuario.getDni()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Ya existe un usuario con DNI: " + usuario.getDni()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(usuario));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar usuario",
               description = "BUENA PRÁCTICA — Soft delete: nunca eliminamos usuarios, solo los desactivamos para mantener trazabilidad e integridad referencial.")
    public ResponseEntity<Usuario> cambiarEstado(
            @PathVariable String id,
            @RequestParam boolean activo) {
        return usuarioService.cambiarEstado(id, activo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    @Operation(summary = "Estado del servicio")
    public Map<String, String> health() {
        return Map.of("servicio", "usuarios", "estado", "activo", "version", "1.0.0");
    }
}
