package com.biblioteca.prestamos.service;

import com.biblioteca.prestamos.model.Prestamo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.biblioteca.prestamos.config.RabbitMQConfig;

import java.util.*;



@Service
public class PrestamoService {

    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    // URLs leídas desde application.properties — LOOSE COUPLING
    @Value("${libros.service.url}")
    private String librosUrl;

    @Value("${usuarios.service.url}")
    private String usuariosUrl;

    // Repositorio en memoria (en producción: base de datos propia del servicio)
    private final Map<String, Prestamo> repositorio = new HashMap<>();
    private int contadorId = 1;

    public PrestamoService(RestTemplate restTemplate, RabbitTemplate rabbitTemplate) {
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    // ── Operación principal: COMPOSICIÓN DE SERVICIOS ─────────────

    public Prestamo crearPrestamo(String usuarioId, String libroId) {

        // PASO 1: Verificar usuario — llamada REMOTA SÍNCRONA
        Map<String, Object> usuario = obtenerUsuario(usuarioId);
        if (!(boolean) usuario.get("activo")) {
            throw new IllegalStateException("El usuario '" + usuarioId + "' está desactivado");
        }

        // PASO 2: Verificar libro — llamada REMOTA SÍNCRONA
        Map<String, Object> libro = obtenerLibro(libroId);
        if (!(boolean) libro.get("disponible")) {
            throw new IllegalStateException(
                "El libro '" + libro.get("titulo") + "' no está disponible para préstamo"
            );
        }

        // PASO 3: Marcar libro como prestado — llamada REMOTA SÍNCRONA (PATCH)
        marcarDisponibilidadLibro(libroId, false);

        // PASO 4: Registrar el préstamo (dato interno del Task Service)
        String prestamoId = "prest-" + String.format("%03d", contadorId++);
        Prestamo prestamo  = new Prestamo(
            prestamoId,
            usuarioId,
            (String) usuario.get("nombre"),
            libroId,
            (String) libro.get("titulo")
        );
        repositorio.put(prestamoId, prestamo);
        // Enviar evento a RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.COLA_EVENTOS, prestamo.toString());
        return prestamo;
    }

    public Prestamo devolverLibro(String prestamoId) {
        Prestamo prestamo = repositorio.get(prestamoId);
        if (prestamo == null) {
            throw new NoSuchElementException("Préstamo '" + prestamoId + "' no encontrado");
        }
        if ("DEVUELTO".equals(prestamo.getEstado())) {
            throw new IllegalStateException("Este préstamo ya fue devuelto");
        }
        // Marcar libro disponible nuevamente — llamada REMOTA
        marcarDisponibilidadLibro(prestamo.getLibroId(), true);
        prestamo.setEstado("DEVUELTO");
        return prestamo;
    }

    public List<Prestamo> listarTodos() {
        return new ArrayList<>(repositorio.values());
    }

    public Optional<Prestamo> buscarPorId(String id) {
        return Optional.ofNullable(repositorio.get(id));
    }

    // ── Métodos privados: comunicación con otros servicios ────────

    /**
     * COMUNICACIÓN SÍNCRONA con el Servicio de Usuarios.
     * RestTemplate.getForObject() hace GET y deserializa JSON → Map.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> obtenerUsuario(String usuarioId) {
        try {
            Map<String, Object> usuario = restTemplate.getForObject(
                usuariosUrl + "/usuarios/" + usuarioId, Map.class
            );
            if (usuario == null) throw new NoSuchElementException("Usuario no encontrado: " + usuarioId);
            return usuario;
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoSuchElementException("Usuario no encontrado: " + usuarioId);
        } catch (ResourceAccessException e) {
            throw new IllegalStateException("Servicio de Usuarios no disponible");
        }
    }

    /**
     * COMUNICACIÓN SÍNCRONA con el Servicio de Libros.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> obtenerLibro(String libroId) {
        try {
            Map<String, Object> libro = restTemplate.getForObject(
                librosUrl + "/libros/" + libroId, Map.class
            );
            if (libro == null) throw new NoSuchElementException("Libro no encontrado: " + libroId);
            return libro;
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoSuchElementException("Libro no encontrado: " + libroId);
        } catch (ResourceAccessException e) {
            throw new IllegalStateException("Servicio de Libros no disponible");
        } catch (Exception e) {
            throw new IllegalStateException("Error al comunicarse con el Servicio de Libros: " + e.getMessage());
        }
    }

    /**
     * PATCH al Servicio de Libros para cambiar disponibilidad.
     * Demuestra que la comunicación SOA no es solo GET:
     * los servicios también se modifican entre sí.
     */
    private void marcarDisponibilidadLibro(String libroId, boolean disponible) {
        try {
            restTemplate.patchForObject(
                librosUrl + "/libros/" + libroId + "/disponibilidad?disponible=" + disponible,
                null, Map.class
            );
        } catch (ResourceAccessException e) {
            throw new IllegalStateException("Servicio de Libros no disponible");
        } catch (Exception e) {
            System.err.println("Error al marcar disponibilidad del libro: " + e.getMessage());
            throw new IllegalStateException("Error al comunicarse con el Servicio de Libros: " + e.getMessage());
        }
    }

    // ── Health: verifica dependencias ────────────────────────────

    public Map<String, String> verificarDependencias() {
        Map<String, String> estado = new HashMap<>();
        try {
            restTemplate.getForObject(librosUrl + "/libros/health", Map.class);
            estado.put("servicio-libros", "ok");
        } catch (Exception e) {
            estado.put("servicio-libros", "error: " + e.getMessage());
        }
        try {
            restTemplate.getForObject(usuariosUrl + "/usuarios/health", Map.class);
            estado.put("servicio-usuarios", "ok");
        } catch (Exception e) {
            estado.put("servicio-usuarios", "error: " + e.getMessage());
        }
        return estado;
    }
}