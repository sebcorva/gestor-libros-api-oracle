package com.duoc.gestor_biblioteca.service;

import java.util.List;
import java.util.Optional;

import com.duoc.gestor_biblioteca.dto.request.LibroRequest;
import com.duoc.gestor_biblioteca.dto.response.LibroResponse;

public interface LibroService {

    List<LibroResponse> getAllLibros();

    Optional<LibroResponse> getLibroById(Long id);

    LibroResponse createLibro(LibroRequest request);

    LibroResponse updateLibro(Long id, LibroRequest request);

    void deleteLibro(Long id);

}
