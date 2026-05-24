package com.biblioteca.prestamos.model;

import java.time.LocalDate;

public class Prestamo {

    private String    id;
    private String    usuarioId;
    private String    usuarioNombre;   // desnormalizado para evitar re-consultas
    private String    libroId;
    private String    libroTitulo;     // desnormalizado para evitar re-consultas
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionEsperada;
    private String    estado;          // "ACTIVO" | "DEVUELTO"

    public Prestamo() {}

    public Prestamo(String id, String usuarioId, String usuarioNombre,
                    String libroId, String libroTitulo) {
        this.id                      = id;
        this.usuarioId               = usuarioId;
        this.usuarioNombre           = usuarioNombre;
        this.libroId                 = libroId;
        this.libroTitulo             = libroTitulo;
        this.fechaPrestamo           = LocalDate.now();
        this.fechaDevolucionEsperada = LocalDate.now().plusDays(14);
        this.estado                  = "ACTIVO";
    }

    public String    getId()                                         { return id; }
    public void      setId(String id)                                { this.id = id; }

    public String    getUsuarioId()                                  { return usuarioId; }
    public void      setUsuarioId(String usuarioId)                  { this.usuarioId = usuarioId; }

    public String    getUsuarioNombre()                              { return usuarioNombre; }
    public void      setUsuarioNombre(String usuarioNombre)          { this.usuarioNombre = usuarioNombre; }

    public String    getLibroId()                                    { return libroId; }
    public void      setLibroId(String libroId)                      { this.libroId = libroId; }

    public String    getLibroTitulo()                                { return libroTitulo; }
    public void      setLibroTitulo(String libroTitulo)              { this.libroTitulo = libroTitulo; }

    public LocalDate getFechaPrestamo()                              { return fechaPrestamo; }
    public void      setFechaPrestamo(LocalDate fechaPrestamo)       { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaDevolucionEsperada()                    { return fechaDevolucionEsperada; }
    public void      setFechaDevolucionEsperada(LocalDate f)         { this.fechaDevolucionEsperada = f; }

    public String    getEstado()                                     { return estado; }
    public void      setEstado(String estado)                        { this.estado = estado; }
}
