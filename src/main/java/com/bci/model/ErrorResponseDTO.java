package com.bci.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.sql.Timestamp;
import java.util.List;

@Value
@Schema(description = "Estructura de respuesta en caso de error")
public class ErrorResponseDTO {
    @Schema(description = "Lista de errores")
    List<Error> error;

    @Value
    @Schema(description = "Detalle del error")
    public static class Error {
        @Schema(description = "Timestamp del error", example = "2025-01-31T12:00:00.000+00:00")
        Timestamp timestamp;

        @Schema(description = "Código de error HTTP", example = "400")
        int codigo;

        @Schema(description = "Descripción del error", example = "Solicitud incorrecta")
        String detail;
    }
}
