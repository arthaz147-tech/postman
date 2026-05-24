package com.biblioteca.usuarios.model;

public class Usuario {

    private String  id;
    private String  nombre;
    private String  email;
    private String  dni;
    private boolean activo;

    public Usuario() {}

    public Usuario(String id, String nombre, String email, String dni, boolean activo) {
        this.id     = id;
        this.nombre = nombre;
        this.email  = email;
        this.dni    = dni;
        this.activo = activo;
    }

    public String  getId()                  { return id; }
    public void    setId(String id)         { this.id = id; }

    public String  getNombre()              { return nombre; }
    public void    setNombre(String nombre) { this.nombre = nombre; }

    public String  getEmail()               { return email; }
    public void    setEmail(String email)   { this.email = email; }

    public String  getDni()                 { return dni; }
    public void    setDni(String dni)       { this.dni = dni; }

    public boolean isActivo()                    { return activo; }
    public void    setActivo(boolean activo)     { this.activo = activo; }
}
