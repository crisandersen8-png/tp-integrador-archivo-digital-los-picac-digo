package ar.edu.unsam.ddso.tp.exception;

/** Excepcion lanzada cuando se intenta registrar un codigo de inventario ya existente. */
public class CodigoInventarioDuplicadoException extends RuntimeException {

    public CodigoInventarioDuplicadoException(String codigoInventario) {
        super("Ya existe una pieza fisica con el codigo de inventario " + codigoInventario);
    }
}
