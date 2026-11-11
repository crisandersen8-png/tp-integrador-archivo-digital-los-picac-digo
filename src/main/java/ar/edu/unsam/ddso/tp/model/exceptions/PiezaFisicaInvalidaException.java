package ar.edu.unsam.ddso.tp.model.exceptions;

/** Excepcion lanzada cuando una pieza fisica viola sus invariantes de negocio. */
public class PiezaFisicaInvalidaException extends RuntimeException {

    public PiezaFisicaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
