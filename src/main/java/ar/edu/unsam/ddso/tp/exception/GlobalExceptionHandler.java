package ar.edu.unsam.ddso.tp.exception;

import ar.edu.unsam.ddso.tp.model.exceptions.DigitalizacionInvalidaException;
import ar.edu.unsam.ddso.tp.model.exceptions.PiezaFisicaInvalidaException;
import ar.edu.unsam.ddso.tp.model.exceptions.RangoFechasInvalidoException;
import ar.edu.unsam.ddso.tp.model.exceptions.TagInvalidoException;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Manejador global de excepciones para traducir errores a respuestas HTTP homogeneas. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
        PiezaFisicaNoEncontradaException.class,
        DigitalizacionNoEncontradaException.class,
        TagNoEncontradoException.class
    })
    public ResponseEntity<Map<String, String>> manejarRecursoNoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({CodigoInventarioDuplicadoException.class, TagDuplicadoException.class})
    public ResponseEntity<Map<String, String>> manejarRecursoDuplicado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarViolacionDeIntegridad(
            DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Violacion de restriccion de unicidad o integridad de datos"));
    }

    @ExceptionHandler({
        PiezaFisicaInvalidaException.class,
        DigitalizacionInvalidaException.class,
        TagInvalidoException.class,
        RangoFechasInvalidoException.class
    })
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

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Map<String, String>> manejarFormatoDeEntradaInvalido(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        Map.of(
                                "error",
                                "El formato de los parametros o del cuerpo JSON es invalido"));
    }
}
