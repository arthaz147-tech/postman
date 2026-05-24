package com.biblioteca.soap.endpoint;

import com.biblioteca.soap.generated.*;
import com.biblioteca.soap.service.LibroRepositorio;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;


@Endpoint
public class LibroEndpoint {

    // Namespace del XSD — debe coincidir exactamente con targetNamespace en libros.xsd
    private static final String NAMESPACE = "http://biblioteca.com/libros";

    private final LibroRepositorio repositorio;

    public LibroEndpoint(LibroRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // ── Operaciones del servicio SOAP ─────────────────────────────

    @PayloadRoot(namespace = NAMESPACE, localPart = "GetLibroRequest")
    @ResponsePayload
    public GetLibroResponse getLibro(@RequestPayload GetLibroRequest request) {
        GetLibroResponse response = new GetLibroResponse();

        repositorio.buscarPorId(request.getId())
                .ifPresentOrElse(
                    response::setLibro,
                    () -> { throw new RuntimeException("Libro no encontrado: " + request.getId()); }
                );

        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "ListarLibrosRequest")
    @ResponsePayload
    public ListarLibrosResponse listarLibros(@RequestPayload ListarLibrosRequest request) {
        ListarLibrosResponse response = new ListarLibrosResponse();
        response.getLibro().addAll(repositorio.listarTodos());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "AgregarLibroRequest")
    @ResponsePayload
    public AgregarLibroResponse agregarLibro(@RequestPayload AgregarLibroRequest request) {
        var libro = repositorio.agregar(
            request.getTitulo(),
            request.getAutor(),
            request.getIsbn(),
            request.isDisponible()
        );

        AgregarLibroResponse response = new AgregarLibroResponse();
        response.setLibro(libro);
        response.setMensaje("Libro registrado exitosamente con ID: " + libro.getId());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "CambiarDisponibilidadRequest")
    @ResponsePayload
    public CambiarDisponibilidadResponse cambiarDisponibilidad(
            @RequestPayload CambiarDisponibilidadRequest request) {

        CambiarDisponibilidadResponse response = new CambiarDisponibilidadResponse();

        repositorio.cambiarDisponibilidad(request.getId(), request.isDisponible())
                .ifPresentOrElse(
                    libro -> {
                        response.setLibro(libro);
                        response.setMensaje("Disponibilidad actualizada a: " + request.isDisponible());
                    },
                    () -> { throw new RuntimeException("Libro no encontrado: " + request.getId()); }
                );

        return response;
    }
}
