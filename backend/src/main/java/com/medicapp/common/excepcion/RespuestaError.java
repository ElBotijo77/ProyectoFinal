package com.medicapp.common.excepcion;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Cuerpo JSON común a todas las respuestas de error de la API.
 *
 * @param fecha   momento en que se produjo el error (UTC, ISO-8601)
 * @param estado  código HTTP numérico
 * @param error   frase estándar del código HTTP (p. ej. "Not Found")
 * @param mensaje descripción legible del problema
 * @param ruta    ruta de la petición que provocó el error
 * @param errores errores de validación campo a campo; se omite si no hay
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RespuestaError(
        Instant fecha,
        int estado,
        String error,
        String mensaje,
        String ruta,
        List<ErrorCampo> errores) {

    /**
     * Error de validación asociado a un campo concreto.
     */
    public record ErrorCampo(String campo, String mensaje) {
    }
}
