package ar.edu.unsam.ddso.tp.exception;

import ar.edu.unsam.ddso.tp.model.exceptions.DigitalizacionInvalidaException;
import ar.edu.unsam.ddso.tp.model.exceptions.PiezaFisicaInvalidaException;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarViolacionDeIntegridad(
            DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Violacion de restriccion de unicidad o integridad de datos"));
    }

    @ExceptionHandler({PiezaFisicaInvalidaException.class, DigitalizacionInvalidaException.class})
    public ResponseEntity<Map<String, String>> manejarErrorDeNegocio(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentoNoValido(
            MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String mensaje =
                fieldError != null && fieldError.getDefaultMessage() != null
                        ? fieldError.getDefaultMessage()
                        : "Datos de entrada invalidos";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", mensaje));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonMalFormado(
            HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        Map.of(
                                "error",
                                "El cuerpo de la solicitud tiene un formato JSON o tipo de dato"
                                        + " invalido"));
    }
}
