package com.bci.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.sql.Timestamp;
import java.util.List;

@Value
@Schema(description = "Estructura de respuesta en caso de error")
public class ErrorResponseDTO {
    @Schema(description = "Mensaje de error", example = "Email already registered")
    String mensaje;
}
