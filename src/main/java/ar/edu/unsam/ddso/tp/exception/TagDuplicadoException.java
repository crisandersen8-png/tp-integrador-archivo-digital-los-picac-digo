package ar.edu.unsam.ddso.tp.exception;

/** Excepcion lanzada cuando se intenta registrar un tag cuyo nombre ya existe. */
public class TagDuplicadoException extends RuntimeException {

    public TagDuplicadoException(String nombre) {
        super("Ya existe un tag con el nombre " + nombre);
    }
}

