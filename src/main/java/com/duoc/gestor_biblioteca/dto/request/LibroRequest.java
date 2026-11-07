package com.duoc.gestor_biblioteca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Datos que entran por HTTP para crear (POST) o actualizar (PUT) un libro.
// No tiene id: lo genera Oracle y en el PUT viaja en la URL.
public record LibroRequest(
        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 150, message = "El titulo no puede superar los 150 caracteres")
        String titulo,

        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 100, message = "El autor no puede superar los 100 caracteres")
        String autor,

        @NotNull(message = "El año de publicacion es obligatorio")
        @Positive(message = "El año de publicacion debe ser positivo")
        Integer anioPublicacion,

        @NotBlank(message = "El genero es obligatorio")
        @Size(max = 100, message = "El genero no puede superar los 100 caracteres")
        String genero) {
}
