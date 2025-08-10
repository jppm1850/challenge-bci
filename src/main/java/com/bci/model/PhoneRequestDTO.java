package com.bci.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Value;

@Value
@Schema(description = "Datos de un teléfono asociado a un usuario")
public class PhoneRequestDTO {

    @NotBlank(message = "El número de teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "El número debe contener solo dígitos y tener entre 7 y 15 caracteres")
    @Schema(description = "Número de teléfono", example = "123456789", required = true)
    String number;

    @NotNull(message = "El código de ciudad es obligatorio")
    @Positive(message = "El código de ciudad debe ser un número positivo")
    @Schema(description = "Código de ciudad", example = "1", required = true)
    Integer citycode;

    @NotBlank(message = "El código de país es obligatorio")
    @Pattern(regexp = "^[0-9]{1,4}$", message = "El código de país debe contener solo dígitos y tener entre 1 y 4 caracteres")
    @Schema(description = "Código de país", example = "57", required = true)
    String countrycode;
}