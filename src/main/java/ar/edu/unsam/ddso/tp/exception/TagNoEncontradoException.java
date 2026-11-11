package ar.edu.unsam.ddso.tp.exception;

/** Excepcion lanzada cuando no se encuentra un tag por su identificador. */
public class TagNoEncontradoException extends RuntimeException {

    public TagNoEncontradoException(Long id) {
        super("Tag con ID " + id + " no encontrado");
    }
}

