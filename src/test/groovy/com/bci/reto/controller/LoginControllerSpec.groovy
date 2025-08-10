package com.bci.reto.controller

import com.bci.controller.LoginController
import com.bci.model.LoginRequestDTO
import com.bci.model.PhoneResponseDTO
import com.bci.model.UserResponseDTO
import com.bci.service.LoginService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.LocalDateTime

class LoginControllerSpec extends Specification {
    LoginService loginService
    LoginController loginController

    def setup() {
        loginService = Mock()
        loginController = new LoginController(loginService)
    }

    def "debería iniciar sesión exitosamente con token válido"() {
        given: 'un header de autorización válido'
        def authHeader = "Bearer token.jwt.valido"

        def phoneResponseDTO = new PhoneResponseDTO("123456789", 1, "57")
        def responseDTO = new UserResponseDTO(
                UUID.randomUUID(),
                "Juan Pérez",
                "juan@ejemplo.com",
                [phoneResponseDTO],
                LocalDateTime.now(),
                LocalDateTime.now(),
                "nuevo.token.jwt",
                true
        )

        def responseEntity = ResponseEntity.ok(responseDTO)

        when: 'se llama al endpoint de login'
        def resultado = loginController.login(authHeader).block()

        then: 'debe procesar el login correctamente'
        1 * loginService.login(authHeader) >> Mono.just(responseEntity)

        resultado.getStatusCode() == HttpStatus.OK
        resultado.getBody() == responseDTO
    }

    def "debería retornar unauthorized para token inválido"() {
        given: 'un header de autorización inválido'
        def authHeader = "Bearer token.invalido"

        def responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        when: 'se llama al endpoint de login'
        def resultado = loginController.login(authHeader).block()

        then: 'debe retornar unauthorized'
        1 * loginService.login(authHeader) >> Mono.just(responseEntity)

        resultado.getStatusCode() == HttpStatus.UNAUTHORIZED
        resultado.getBody() == null
    }

    def "debería retornar unauthorized para header malformado"() {
        given: 'un header malformado'
        def authHeader = "InvalidHeader"

        def responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        when: 'se llama al endpoint de login'
        def resultado = loginController.login(authHeader).block()

        then: 'debe retornar unauthorized'
        1 * loginService.login(authHeader) >> Mono.just(responseEntity)

        resultado.getStatusCode() == HttpStatus.UNAUTHORIZED
        resultado.getBody() == null
    }

    // ==================== PRUEBAS PARA LOGIN CON CREDENCIALES ====================

    def "debería iniciar sesión exitosamente con credenciales válidas"() {
        given: 'credenciales válidas'
        def request = new LoginRequestDTO("juan@ejemplo.com", "Password1a2")

        def phoneResponseDTO = new PhoneResponseDTO("987654321", 2, "58")
        def responseDTO = new UserResponseDTO(
                UUID.randomUUID(),
                "Juan Pérez",
                "juan@ejemplo.com",
                [phoneResponseDTO],
                LocalDateTime.now(),
                LocalDateTime.now(),
                "nuevo.token.jwt",
                true
        )

        def responseEntity = ResponseEntity.ok(responseDTO)

        when: 'se llama al endpoint de login con credenciales'
        def resultado = loginController.loginWithCredentials(request).block()

        then: 'debe procesar el login correctamente'
        1 * loginService.loginWithCredentials(request) >> Mono.just(responseEntity)

        resultado.getStatusCode() == HttpStatus.OK
        resultado.getBody() == responseDTO
    }

    def "debería retornar not found cuando el usuario no existe"() {
        given: 'credenciales de usuario inexistente'
        def request = new LoginRequestDTO("noexiste@ejemplo.com", "Password1a2")

        def responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)

        when: 'se llama al endpoint de login con credenciales'
        def resultado = loginController.loginWithCredentials(request).block()

        then: 'debe retornar not found'
        1 * loginService.loginWithCredentials(request) >> Mono.just(responseEntity)

        resultado.getStatusCode() == HttpStatus.NOT_FOUND
        resultado.getBody() == null
    }

    def "debería retornar bad request para contraseña incorrecta"() {
        given: 'credenciales con contraseña incorrecta'
        def request = new LoginRequestDTO("juan@ejemplo.com", "contraseñaIncorrecta")

        def responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)

        when: 'se llama al endpoint de login con credenciales'
        def resultado = loginController.loginWithCredentials(request).block()

        then: 'debe retornar bad request'
        1 * loginService.loginWithCredentials(request) >> Mono.just(responseEntity)

        resultado.getStatusCode() == HttpStatus.BAD_REQUEST
        resultado.getBody() == null
    }

    def "debería retornar unauthorized para credenciales inválidas"() {
        given: 'credenciales inválidas'
        def request = new LoginRequestDTO("juan@ejemplo.com", "Password1a2")

        def responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        when: 'se llama al endpoint de login con credenciales'
        def resultado = loginController.loginWithCredentials(request).block()

        then: 'debe retornar unauthorized'
        1 * loginService.loginWithCredentials(request) >> Mono.just(responseEntity)

        resultado.getStatusCode() == HttpStatus.UNAUTHORIZED
        resultado.getBody() == null
    }

    def "debería procesar login exitoso sin teléfonos"() {
        given: 'credenciales válidas de usuario sin teléfonos'
        def request = new LoginRequestDTO("juan@ejemplo.com", "Password1a2")

        def responseDTO = new UserResponseDTO(
                UUID.randomUUID(),
                "Juan Pérez",
                "juan@ejemplo.com",
                [], // Sin teléfonos
                LocalDateTime.now(),
                LocalDateTime.now(),
                "nuevo.token.jwt",
                true
        )

        def responseEntity = ResponseEntity.ok(responseDTO)

        when: 'se llama al endpoint de login con credenciales'
        def resultado = loginController.loginWithCredentials(request).block()

        then: 'debe procesar el login correctamente'
        1 * loginService.loginWithCredentials(request) >> Mono.just(responseEntity)

        resultado.getStatusCode() == HttpStatus.OK
        resultado.getBody() == responseDTO
        resultado.getBody().getPhones().isEmpty()
    }

    def "debería manejar múltiples teléfonos en respuesta de login"() {
        given: 'credenciales válidas de usuario con múltiples teléfonos'
        def request = new LoginRequestDTO("juan@ejemplo.com", "Password1a2")

        def phone1 = new PhoneResponseDTO("123456789", 1, "57")
        def phone2 = new PhoneResponseDTO("987654321", 2, "58")

        def responseDTO = new UserResponseDTO(
                UUID.randomUUID(),
                "Juan Pérez",
                "juan@ejemplo.com",
                [phone1, phone2],
                LocalDateTime.now(),
                LocalDateTime.now(),
                "nuevo.token.jwt",
                true
        )

        def responseEntity = ResponseEntity.ok(responseDTO)

        when: 'se llama al endpoint de login con credenciales'
        def resultado = loginController.loginWithCredentials(request).block()

        then: 'debe procesar el login correctamente'
        1 * loginService.loginWithCredentials(request) >> Mono.just(responseEntity)

        resultado.getStatusCode() == HttpStatus.OK
        resultado.getBody() == responseDTO
        resultado.getBody().getPhones().size() == 2
    }
}