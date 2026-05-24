package com.biblioteca.usuarios.service;

import com.biblioteca.usuarios.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UsuarioService {

    private final Map<String, Usuario> repositorio = new HashMap<>();
    private int contadorId = 4;

    public UsuarioService() {
        repositorio.put("usr-001", new Usuario("usr-001", "Ana García",   "ana@biblioteca.pe",   "12345678", true));
        repositorio.put("usr-002", new Usuario("usr-002", "Luis Pérez",   "luis@biblioteca.pe",  "87654321", true));
        repositorio.put("usr-003", new Usuario("usr-003", "María Quispe", "maria@biblioteca.pe", "11223344", true));
    }

    public List<Usuario> listarTodos() {
        return new ArrayList<>(repositorio.values());
    }

    public Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(repositorio.get(id));
    }

    public Optional<String> buscarIdPorDni(String dni) {
        return repositorio.values().stream()
                .filter(u -> u.getDni().equals(dni))
                .map(Usuario::getId)
                .findFirst();
    }

    public Usuario crear(Usuario usuario) {
        String nuevoId = "usr-" + String.format("%03d", contadorId++);
        usuario.setId(nuevoId);
        usuario.setActivo(true);
        repositorio.put(nuevoId, usuario);
        return usuario;
    }

    public Optional<Usuario> cambiarEstado(String id, boolean activo) {
        Usuario usuario = repositorio.get(id);
        if (usuario == null) return Optional.empty();
        usuario.setActivo(activo);
        return Optional.of(usuario);
    }
}
