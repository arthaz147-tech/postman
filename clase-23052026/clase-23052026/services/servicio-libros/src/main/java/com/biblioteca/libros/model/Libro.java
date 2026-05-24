package com.biblioteca.libros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// CONCEPTO SOA: @Entity convierte esta clase en una tabla PostgreSQL.
// El servicio es dueño de su propio esquema de datos — database per service.
@Entity
@Table(name = "libro")
public class Libro {

    @Id
    private String id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    @Column(unique = true)
    private String isbn;

    private boolean disponible;

    // Constructor vacío requerido para deserialización JSON
    public Libro() {}

    public Libro(String id, String titulo, String autor, String isbn, boolean disponible) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.disponible = disponible;
    }

    // Getters y Setters (contrato de datos)
    public String getId()                  { return id; }
    public void   setId(String id)         { this.id = id; }

    public String getTitulo()              { return titulo; }
    public void   setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor()               { return autor; }
    public void   setAutor(String autor)   { this.autor = autor; }

    public String getIsbn()                { return isbn; }
    public void   setIsbn(String isbn)     { this.isbn = isbn; }

    public boolean isDisponible()                    { return disponible; }
    public void    setDisponible(boolean disponible) { this.disponible = disponible; }
}
