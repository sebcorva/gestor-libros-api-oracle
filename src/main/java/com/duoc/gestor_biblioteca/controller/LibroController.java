package com.duoc.gestor_biblioteca.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.gestor_biblioteca.dto.request.LibroRequest;
import com.duoc.gestor_biblioteca.dto.response.LibroResponse;
import com.duoc.gestor_biblioteca.service.LibroService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/libros")
@RequiredArgsConstructor
public class LibroController {

    private final LibroService libroService;

    @GetMapping
    public ResponseEntity<List<LibroResponse>> getAllLibros() {
        return ResponseEntity.ok(libroService.getAllLibros());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibroResponse> getLibroById(@PathVariable Long id) {
        return ResponseEntity.ok(libroService.getLibroById(id));
    }

    @PostMapping
    public ResponseEntity<LibroResponse> createLibro(@Valid @RequestBody LibroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libroService.createLibro(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LibroResponse> updateLibro(@PathVariable Long id, @Valid @RequestBody LibroRequest request) {
        return ResponseEntity.ok(libroService.updateLibro(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLibro(@PathVariable Long id) {
        libroService.deleteLibro(id);
        return ResponseEntity.noContent().build();
    }
}
