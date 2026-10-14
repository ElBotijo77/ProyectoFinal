package com.medicapp.common.excepcion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;

/**
 * Traduce las excepciones lanzadas por los controladores a respuestas
 * {@link RespuestaError} con un formato JSON uniforme.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> recursoNoEncontrado(RecursoNoEncontradoException ex,
                                                              HttpServletRequest peticion) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), peticion, List.of());
    }

    // Fallos de @Valid sobre el cuerpo de la petición
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> validacionCuerpo(MethodArgumentNotValidException ex,
                                                           HttpServletRequest peticion) {
        List<RespuestaError.ErrorCampo> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new RespuestaError.ErrorCampo(e.getField(), e.getDefaultMessage()))
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "La petición contiene datos no válidos", peticion, errores);
    }

    // Fallos de validación sobre parámetros (@RequestParam, @PathVariable) en clases @Validated
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RespuestaError> validacionParametros(ConstraintViolationException ex,
                                                               HttpServletRequest peticion) {
        List<RespuestaError.ErrorCampo> errores = ex.getConstraintViolations().stream()
                .map(v -> new RespuestaError.ErrorCampo(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "La petición contiene datos no válidos", peticion, errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> cuerpoIlegible(HttpMessageNotReadableException ex,
                                                         HttpServletRequest peticion) {
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido", peticion, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaError> tipoIncorrecto(MethodArgumentTypeMismatchException ex,
                                                         HttpServletRequest peticion) {
        String mensaje = "El parámetro '%s' tiene un valor no válido".formatted(ex.getName());
        return construir(HttpStatus.BAD_REQUEST, mensaje, peticion, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RespuestaError> parametroAusente(MissingServletRequestParameterException ex,
                                                           HttpServletRequest peticion) {
        String mensaje = "Falta el parámetro obligatorio '%s'".formatted(ex.getParameterName());
        return construir(HttpStatus.BAD_REQUEST, mensaje, peticion, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RespuestaError> rutaNoEncontrada(NoResourceFoundException ex,
                                                           HttpServletRequest peticion) {
        return construir(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", peticion, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RespuestaError> metodoNoPermitido(HttpRequestMethodNotSupportedException ex,
                                                            HttpServletRequest peticion) {
        String mensaje = "El método %s no está permitido en esta ruta".formatted(ex.getMethod());
        return construir(HttpStatus.METHOD_NOT_ALLOWED, mensaje, peticion, List.of());
    }

    // Último recurso: se registra la traza completa y no se expone el detalle al cliente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> errorInesperado(Exception ex, HttpServletRequest peticion) {
        log.error("Error no controlado en {} {}", peticion.getMethod(), peticion.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Se ha producido un error interno", peticion, List.of());
    }

    private ResponseEntity<RespuestaError> construir(HttpStatus estado, String mensaje,
                                                     HttpServletRequest peticion,
                                                     List<RespuestaError.ErrorCampo> errores) {
        RespuestaError cuerpo = new RespuestaError(
                Instant.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                peticion.getRequestURI(),
                errores);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
