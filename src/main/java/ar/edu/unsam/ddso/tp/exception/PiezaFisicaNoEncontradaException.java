package ar.edu.unsam.ddso.tp.exception;

/** Excepcion lanzada cuando no se encuentra una pieza fisica por su identificador. */
public class PiezaFisicaNoEncontradaException extends RuntimeException {

    public PiezaFisicaNoEncontradaException(Long id) {
        super("Pieza fisica con ID " + id + " no encontrada");
    }

    public PiezaFisicaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
