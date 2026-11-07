package com.duoc.gestor_biblioteca.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.duoc.gestor_biblioteca.model.Libro;
import com.duoc.gestor_biblioteca.repository.LibroRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LibroServiceImpl implements LibroService {

    private final LibroRepository libroRepository;

    @Override
    public List<Libro> getAllLibros() {
        return libroRepository.findAll();
    }

    @Override
    public Optional<Libro> getLibroById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id no puede ser nulo o menor a 0");
        }
        return libroRepository.findById(id);
    }

    @Override
    public Libro createLibro(Libro libro) {
        if (libro == null) {
            throw new IllegalArgumentException("El libro no puede ser nulo");
        }
        return libroRepository.save(libro);
    }

    @Override
    public Libro updateLibro(Long id, Libro libro) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id no puede ser nulo o menor a 0");
        }

        Optional<Libro> libroExistente = libroRepository.findById(id);

        if (libroExistente.isEmpty()) {
            throw new IllegalArgumentException("El libro no existe");
        } else if (libro == null) {
            throw new IllegalArgumentException("El libro no puede ser nulo");
        }

        Libro libroActualizar = libroExistente.get();

        libroActualizar.setTitulo(libro.getTitulo());
        libroActualizar.setAutor(libro.getAutor());
        libroActualizar.setAnioPublicacion(libro.getAnioPublicacion());
        libroActualizar.setGenero(libro.getGenero());

        return libroRepository.save(libroActualizar);
    }

    @Override
    public void deleteLibro(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id no puede ser nulo o menor a 0");
        }

        libroRepository.deleteById(id);
    }

}
