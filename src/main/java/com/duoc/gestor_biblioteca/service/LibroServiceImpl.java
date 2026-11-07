package com.duoc.gestor_biblioteca.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.duoc.gestor_biblioteca.dto.mapper.LibroMapper;
import com.duoc.gestor_biblioteca.dto.request.LibroRequest;
import com.duoc.gestor_biblioteca.dto.response.LibroResponse;
import com.duoc.gestor_biblioteca.exception.LibroNoEncontradoException;
import com.duoc.gestor_biblioteca.model.Libro;
import com.duoc.gestor_biblioteca.repository.LibroRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LibroServiceImpl implements LibroService {

    private final LibroRepository libroRepository;

    @Override
    public List<LibroResponse> getAllLibros() {
        return libroRepository.findAll().stream()
                .map(LibroMapper::toResponse)
                .toList();
    }

    @Override
    public LibroResponse getLibroById(Long id) {
        return LibroMapper.toResponse(buscarLibro(id));
    }

    @Override
    public LibroResponse createLibro(LibroRequest request) {
        Libro libro = LibroMapper.toEntity(request);
        return LibroMapper.toResponse(libroRepository.save(libro));
    }

    @Override
    public LibroResponse updateLibro(Long id, LibroRequest request) {
        Libro libroActualizar = buscarLibro(id);
        LibroMapper.updateEntity(libroActualizar, request);
        return LibroMapper.toResponse(libroRepository.save(libroActualizar));
    }

    @Override
    public void deleteLibro(Long id) {
        if (!libroRepository.existsById(id)) {
            throw new LibroNoEncontradoException(id);
        }
        libroRepository.deleteById(id);
    }

    private Libro buscarLibro(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new LibroNoEncontradoException(id));
    }

}
