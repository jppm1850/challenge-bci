package com.bci.reto.controller

import com.bci.controller.UserController
import com.bci.model.PhoneRequestDTO
import com.bci.model.UserResponseDTO
import com.bci.model.UserSignUpRequestDTO
import com.bci.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.LocalDateTime

class UserControllerSpec extends Specification {
    UserService userService
    UserController userController

    def setup() {
        userService = Mock()
        userController = new UserController(userService)
    }

    def "debería registrar un usuario exitosamente"() {
        given: 'una solicitud de registro válida'
        def phoneDto = new PhoneRequestDTO("123456789", 1, "57")
        def request = new UserSignUpRequestDTO("Juan Pérez", "juan@ejemplo.com", "Password1a2", [phoneDto])

        def responseDTO = new UserResponseDTO(
                UUID.randomUUID(),
                "Juan Pérez",
                "juan@ejemplo.com",
                [],
                LocalDateTime.now(),
                null,
                "jwt.token",
                true
        )

        def responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(responseDTO)

        when: 'se llama al endpoint de registro'
        def resultado = userController.signUp(request).block()

        then: 'debe procesar la solicitud correctamente'
        1 * userService.signUp(request) >> Mono.just(responseEntity)

        resultado.getStatusCode() == HttpStatus.CREATED
        resultado.getBody() == responseDTO
    }

    def "debería manejar errores en el registro"() {
        given: 'una solicitud que causará error'
        def request = new UserSignUpRequestDTO("Juan Pérez", "existente@ejemplo.com", "Password1a2", [])

        def errorResponse = ResponseEntity.status(HttpStatus.CONFLICT).body("Error message")

        when: 'se llama al endpoint de registro'
        def resultado = userController.signUp(request).block()

        then: 'debe retornar el error correspondiente'
        1 * userService.signUp(request) >> Mono.just(errorResponse)

        resultado.getStatusCode() == HttpStatus.CONFLICT
    }

    def "debería procesar solicitud con teléfonos null"() {
        given: 'una solicitud con teléfonos null'
        def request = new UserSignUpRequestDTO("Juan Pérez", "juan@ejemplo.com", "Password1a2", null)

        def responseDTO = new UserResponseDTO(
                UUID.randomUUID(),
                "Juan Pérez",
                "juan@ejemplo.com",
                [],
                LocalDateTime.now(),
                null,
                "jwt.token",
                true
        )

        def responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(responseDTO)

        when: 'se llama al endpoint de registro'
        def resultado = userController.signUp(request).block()

        then: 'debe procesar la solicitud correctamente'
        1 * userService.signUp(request) >> Mono.just(responseEntity)

        resultado.getStatusCode() == HttpStatus.CREATED
        resultado.getBody() == responseDTO
    }
}