package com.duoc.gestor_biblioteca.dto.mapper;

import com.duoc.gestor_biblioteca.dto.request.LibroRequest;
import com.duoc.gestor_biblioteca.dto.response.LibroResponse;
import com.duoc.gestor_biblioteca.model.Libro;

public final class LibroMapper {

    private LibroMapper() {
    }

    public static Libro toEntity(LibroRequest request) {
        Libro libro = new Libro();
        updateEntity(libro, request);
        return libro;
    }

    public static LibroResponse toResponse(Libro libro) {
        return new LibroResponse(
                libro.getId(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getAnioPublicacion(),
                libro.getGenero());
    }

    public static void updateEntity(Libro libro, LibroRequest request) {
        libro.setTitulo(request.titulo());
        libro.setAutor(request.autor());
        libro.setAnioPublicacion(request.anioPublicacion());
        libro.setGenero(request.genero());
    }
}
