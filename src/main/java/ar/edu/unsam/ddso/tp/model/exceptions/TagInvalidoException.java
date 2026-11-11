package ar.edu.unsam.ddso.tp.model.exceptions;

/** Excepcion lanzada cuando un tag viola sus invariantes de negocio. */
public class TagInvalidoException extends RuntimeException {

    public TagInvalidoException(String mensaje) {
        super(mensaje);
    }
}

