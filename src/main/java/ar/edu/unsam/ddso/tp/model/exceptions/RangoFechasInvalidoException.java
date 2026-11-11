package ar.edu.unsam.ddso.tp.model.exceptions;

/** Excepcion lanzada cuando la fecha inicial de busqueda es posterior a la fecha final. */
public class RangoFechasInvalidoException extends RuntimeException {

    public RangoFechasInvalidoException(String mensaje) {
        super(mensaje);
    }
}

