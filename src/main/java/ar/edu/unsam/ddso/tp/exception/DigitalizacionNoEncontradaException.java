package ar.edu.unsam.ddso.tp.exception;

/** Excepcion lanzada cuando no se encuentra una digitalizacion por su identificador. */
public class DigitalizacionNoEncontradaException extends RuntimeException {

    public DigitalizacionNoEncontradaException(Long id) {
        super("Digitalizacion con ID " + id + " no encontrada");
    }
}

