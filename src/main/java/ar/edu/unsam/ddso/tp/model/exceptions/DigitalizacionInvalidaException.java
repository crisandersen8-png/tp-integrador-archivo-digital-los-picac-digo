package ar.edu.unsam.ddso.tp.model.exceptions;

/** Excepcion lanzada cuando una digitalizacion viola sus invariantes de negocio. */
public class DigitalizacionInvalidaException extends RuntimeException {

    public DigitalizacionInvalidaException(String mensaje) {
        super(mensaje);
    }

    public DigitalizacionInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
