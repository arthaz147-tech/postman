package com.biblioteca.libros.service;

import com.biblioteca.libros.model.Libro;
import com.biblioteca.libros.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LibroService {

    // ANTES: Map<String, Libro> en memoria — datos se pierden al reiniciar
    // AHORA: JpaRepository → PostgreSQL — datos persisten entre reinicios
    @Autowired
    private LibroRepository repositorio;

    public List<Libro> listarTodos() {
        return repositorio.findAll();
    }

    public Optional<Libro> buscarPorId(String id) {
        return repositorio.findById(id);
    }

    public Libro crear(Libro libro) {
        // UUID garantiza IDs únicos sin conflictos al reiniciar el servicio
        libro.setId(UUID.randomUUID().toString());
        return repositorio.save(libro);
    }

    public Optional<Libro> cambiarDisponibilidad(String id, boolean disponible) {
        return repositorio.findById(id).map(libro -> {
            libro.setDisponible(disponible);
            return repositorio.save(libro);
        });
    }

    public boolean eliminar(String id) {
        if (!repositorio.existsById(id)) return false;
        repositorio.deleteById(id);
        return true;
    }
}
