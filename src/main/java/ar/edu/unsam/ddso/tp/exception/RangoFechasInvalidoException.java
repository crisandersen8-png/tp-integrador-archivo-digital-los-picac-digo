package ar.edu.unsam.ddso.tp.exception;

/** Excepcion lanzada cuando el parametro fechaDesde es posterior a fechaHasta en una consulta. */
public class RangoFechasInvalidoException extends RuntimeException {

    public RangoFechasInvalidoException(String mensaje) {
        super(mensaje);
    }
}
