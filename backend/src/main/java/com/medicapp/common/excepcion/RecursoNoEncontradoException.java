package com.medicapp.common.excepcion;

/**
 * Se lanza desde los servicios cuando el recurso solicitado no existe.
 * El manejador global la traduce a un 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
