package com.duoc.gestor_biblioteca.service;

import java.util.List;

import com.duoc.gestor_biblioteca.dto.request.LibroRequest;
import com.duoc.gestor_biblioteca.dto.response.LibroResponse;

public interface LibroService {

    List<LibroResponse> getAllLibros();

    LibroResponse getLibroById(Long id);

    LibroResponse createLibro(LibroRequest request);

    LibroResponse updateLibro(Long id, LibroRequest request);

    void deleteLibro(Long id);

}
