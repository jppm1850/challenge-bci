package com.bci.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.List;

@Value
@Schema(description = "Datos para el registro de usuario")
public class UserSignUpRequestDTO {

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Schema(description = "Nombre del usuario", example = "Juan Pérez", required = true)
        String name;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        @Schema(description = "Correo electrónico", example = "juan.perez@example.com", required = true)
        String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=(?:[^0-9]*[0-9]){2}[^0-9]*$)(?!.*[0-9]{2})[A-Za-z0-9]{8,12}$",
                message = "La contraseña debe tener una letra mayúscula, una minúscula, exactamente dos números no consecutivos y tener entre 8 y 12 caracteres"
        )
        @Schema(
                description = "Contraseña del usuario (Debe tener al menos una mayúscula, una minúscula y exactamente dos números no consecutivos)",
                example = "Password1a2",
                required = true
        )
        String password;

        @Valid
        @Schema(description = "Lista de teléfonos asociados al usuario")
        List<PhoneRequestDTO> phones;
}