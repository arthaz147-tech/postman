package com.biblioteca.soap.service;

import com.biblioteca.soap.generated.Libro;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LibroRepositorio {

    private final Map<String, Libro> datos = new HashMap<>();
    private int contador = 4;

    public LibroRepositorio() {
        datos.put("lib-001", crearLibro("lib-001", "Clean Architecture",     "Robert C. Martin", "978-0134494166", true));
        datos.put("lib-002", crearLibro("lib-002", "Designing SOA",          "Thomas Erl",       "978-0137135790", true));
        datos.put("lib-003", crearLibro("lib-003", "Building Microservices", "Sam Newman",       "978-1492034025", true));
    }

    public Optional<Libro> buscarPorId(String id) {
        return Optional.ofNullable(datos.get(id));
    }

    public List<Libro> listarTodos() {
        return new ArrayList<>(datos.values());
    }

    public Libro agregar(String titulo, String autor, String isbn, boolean disponible) {
        String id = "lib-" + String.format("%03d", contador++);
        Libro libro = crearLibro(id, titulo, autor, isbn, disponible);
        datos.put(id, libro);
        return libro;
    }

    public Optional<Libro> cambiarDisponibilidad(String id, boolean disponible) {
        Libro libro = datos.get(id);
        if (libro == null) return Optional.empty();
        libro.setDisponible(disponible);
        return Optional.of(libro);
    }

    private Libro crearLibro(String id, String titulo, String autor, String isbn, boolean disponible) {
        Libro libro = new Libro();
        libro.setId(id);
        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setIsbn(isbn);
        libro.setDisponible(disponible);
        return libro;
    }
}
