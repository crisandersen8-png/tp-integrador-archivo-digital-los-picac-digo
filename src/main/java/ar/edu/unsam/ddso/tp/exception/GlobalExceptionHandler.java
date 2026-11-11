package ar.edu.unsam.ddso.tp.exception;

import ar.edu.unsam.ddso.tp.model.exceptions.DigitalizacionInvalidaException;
import ar.edu.unsam.ddso.tp.model.exceptions.PiezaFisicaInvalidaException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Manejador global de excepciones para traducir errores a respuestas HTTP homogeneas. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PiezaFisicaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> manejarPiezaNoEncontrada(
            PiezaFisicaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CodigoInventarioDuplicadoException.class)
    public ResponseEntity<Map<String, String>> manejarCodigoDuplicado(
            CodigoInventarioDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({PiezaFisicaInvalidaException.class, DigitalizacionInvalidaException.class})
    public ResponseEntity<Map<String, String>> manejarErrorDeNegocio(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
