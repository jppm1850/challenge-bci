package com.bci.controller;


import com.bci.model.ErrorResponseDTO;
import com.bci.model.LoginRequestDTO;
import com.bci.model.UserResponseDTO;
import com.bci.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/login")
@Tag(name = "Login", description = "APIs para gestión de login")
public class LoginController {

    private final LoginService loginService;

    @Operation(
            summary = "Iniciar sesión",
            description = "Permite a un usuario autenticarse con un token JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inicio de sesión exitoso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado - token inválido o expirado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud incorrecta - header Authorization requerido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> login(
            @RequestHeader("Authorization") String authHeader) {
        log.info("Iniciando proceso de login");
        return loginService.login(authHeader)
                .doOnSuccess(response -> log.info("Login exitoso"))
                .doOnError(error -> log.error("Error durante el login: {}", error.getMessage()));
    }

    @Operation(
            summary = "Iniciar sesión con credenciales",
            description = "Permite a un usuario autenticarse usando email y contraseña. Genera un nuevo token JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inicio de sesión exitoso - token generado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud incorrecta - datos inválidos o contraseña incorrecta",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado - credenciales inválidas",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping(value = "/authenticate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> loginWithCredentials(
            @Valid @RequestBody LoginRequestDTO request) {
        log.info("Iniciando proceso de login con credenciales para email: {}", request.getEmail());
        return loginService.loginWithCredentials(request)
                .doOnSuccess(response -> log.info("Login con credenciales exitoso para email: {}", request.getEmail()))
                .doOnError(error -> log.error("Error durante el login con credenciales: {}", error.getMessage()));
    }
}