package com.duoc.gestor_biblioteca.dto.response;

public record LibroResponse(
        Long id,
        String titulo,
        String autor,
        Integer anioPublicacion,
        String genero) {
}
