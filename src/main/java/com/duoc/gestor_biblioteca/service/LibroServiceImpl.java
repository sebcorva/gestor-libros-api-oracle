package com.duoc.gestor_biblioteca.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.duoc.gestor_biblioteca.dto.mapper.LibroMapper;
import com.duoc.gestor_biblioteca.dto.request.LibroRequest;
import com.duoc.gestor_biblioteca.dto.response.LibroResponse;
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
    public Optional<LibroResponse> getLibroById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id no puede ser nulo o menor a 0");
        }
        return libroRepository.findById(id).map(LibroMapper::toResponse);
    }

    @Override
    public LibroResponse createLibro(LibroRequest request) {
        Libro libro = LibroMapper.toEntity(request);
        return LibroMapper.toResponse(libroRepository.save(libro));
    }

    @Override
    public LibroResponse updateLibro(Long id, LibroRequest request) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id no puede ser nulo o menor a 0");
        }

        Optional<Libro> libroExistente = libroRepository.findById(id);

        if (libroExistente.isEmpty()) {
            throw new IllegalArgumentException("El libro no existe");
        }

        Libro libroActualizar = libroExistente.get();
        LibroMapper.updateEntity(libroActualizar, request);

        return LibroMapper.toResponse(libroRepository.save(libroActualizar));
    }

    @Override
    public void deleteLibro(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id no puede ser nulo o menor a 0");
        }

        libroRepository.deleteById(id);
    }

}
