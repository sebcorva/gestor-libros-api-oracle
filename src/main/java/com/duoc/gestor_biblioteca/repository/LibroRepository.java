package com.duoc.gestor_biblioteca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duoc.gestor_biblioteca.model.Libro;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
}
