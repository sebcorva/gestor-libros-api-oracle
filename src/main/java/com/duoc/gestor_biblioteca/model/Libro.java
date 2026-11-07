package com.duoc.gestor_biblioteca.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "titulo")
    @NotBlank(message = "El titulo no puede estar vacio")
    private String titulo;

    @Column(name = "autor")
    @NotBlank(message = "El autor no puede estar vacio")
    private String autor;

    @Column(name = "anio_publicacion")
    @NotNull(message = "El anio de publicacion no puede ser nulo")
    private Integer anioPublicacion;

    @Column(name = "genero")
    @NotBlank(message = "El genero no puede estar vacio")
    private String genero;

}
