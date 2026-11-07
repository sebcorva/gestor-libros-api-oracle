package com.duoc.gestor_biblioteca.exception;

public class LibroNoEncontradoException extends RuntimeException {

    public LibroNoEncontradoException(Long id) {
        super("No existe el libro con id " + id);
    }
}
