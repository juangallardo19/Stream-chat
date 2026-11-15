package com.streamchat.typing_api.dto;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record ErrorResponse(
        Instant timestamp,
        int codigoHttp,
        String estado,
        String mensaje,
        String ruta,
        Map<String, String> detallesValidacion) {

    public ErrorResponse {
        Objects.requireNonNull(timestamp, "La hora del error es obligatoria");
        Objects.requireNonNull(estado, "El estado HTTP es obligatorio");
        Objects.requireNonNull(mensaje, "El mensaje de error es obligatorio");
        Objects.requireNonNull(ruta, "La ruta es obligatoria");
        detallesValidacion = Map.copyOf(detallesValidacion);
    }

    public static ErrorResponse deError(Instant timestamp, int codigoHttp, String estado, String mensaje, String ruta) {
        return new ErrorResponse(timestamp, codigoHttp, estado, mensaje, ruta, Map.of());
    }

    public static ErrorResponse deValidacion(Instant timestamp, int codigoHttp, String estado, String mensaje,
            String ruta, Map<String, String> errores) {
        return new ErrorResponse(timestamp, codigoHttp, estado, mensaje, ruta, errores);
    }
}
