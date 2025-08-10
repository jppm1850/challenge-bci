package com.bci.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Value
@Schema(description = "Datos de respuesta de un teléfono")
public class PhoneResponseDTO {
    @Schema(description = "Número de teléfono", example = "123456789")
    String number;

    @Schema(description = "Código de ciudad", example = "1")
    Integer citycode;

    @Schema(description = "Código de país", example = "57")
    String countrycode;
}
