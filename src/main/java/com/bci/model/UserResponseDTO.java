package com.bci.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Schema(description = "Respuesta con los datos del usuario registrado o autenticado")
public class UserResponseDTO {
    @Schema(description = "Identificador único del usuario", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id;

    @Schema(description = "Nombre del usuario", example = "Juan Pérez")
    String name;

    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@example.com")
    String email;

    @Schema(description = "Lista de teléfonos")
    List<PhoneResponseDTO> phones;

    @Schema(description = "Fecha de creación del usuario")
    LocalDateTime created;

    @Schema(description = "Última fecha de inicio de sesión")
    LocalDateTime lastLogin;

    @Schema(description = "Token de autenticación")
    String token;

    @Schema(description = "Estado del usuario", example = "true")
    Boolean isActive;
}
