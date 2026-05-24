package com.biblioteca.libros.repository;

import com.biblioteca.libros.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// CONCEPTO SOA: el repositorio abstrae el acceso a la base de datos.
// JpaRepository provee findAll, findById, save, deleteById sin código extra.
// El servicio no sabe si los datos vienen de PostgreSQL, MySQL u Oracle.
@Repository
public interface LibroRepository extends JpaRepository<Libro, String> {
}
