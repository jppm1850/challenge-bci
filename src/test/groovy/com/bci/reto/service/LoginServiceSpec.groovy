package com.bci.reto.service

import com.bci.entity.Phone
import com.bci.entity.User
import com.bci.exception.UserNotFoundException
import com.bci.exception.ValidationException
import com.bci.mapper.UserMapper
import com.bci.model.LoginRequestDTO
import com.bci.model.PhoneResponseDTO
import com.bci.model.UserResponseDTO
import com.bci.repository.PhoneRepository
import com.bci.repository.UserRepository
import com.bci.service.JwtService
import com.bci.service.LoginService
import com.bci.service.impl.LoginServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.LocalDateTime

class LoginServiceSpec extends Specification {
    UserRepository userRepository
    PhoneRepository phoneRepository
    PasswordEncoder passwordEncoder
    JwtService jwtService
    UserMapper userMapper
    LoginService loginService

    def setup() {
        userRepository = Mock()
        phoneRepository = Mock()
        passwordEncoder = Mock()
        jwtService = Mock()
        userMapper = Mock()
        loginService = new LoginServiceImpl(userRepository, phoneRepository, passwordEncoder, jwtService, userMapper)
    }

    def "debería iniciar sesión exitosamente con token válido"() {
        given: 'un token válido'
        def authHeader = "Bearer token.jwt.valido"
        def token = "token.jwt.valido"
        def email = "juan@ejemplo.com"

        def user = new User()
        user.setId(UUID.randomUUID())
        user.setName("Juan Pérez")
        user.setEmail(email)
        user.setCreated(LocalDateTime.now())
        user.setLastLogin(LocalDateTime.now().minusDays(1))
        user.setIsActive(true)

        def updatedUser = new User()
        updatedUser.setId(user.getId())
        updatedUser.setName(user.getName())
        updatedUser.setEmail(user.getEmail())
        updatedUser.setCreated(user.getCreated())
        updatedUser.setLastLogin(LocalDateTime.now())
        updatedUser.setToken("nuevo.token.jwt")
        updatedUser.setIsActive(true)

        def phone = new Phone()
        phone.setNumber("123456789")
        phone.setCitycode(1)
        phone.setCountrycode("57")

        def responseDTO = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                [new PhoneResponseDTO(phone.getNumber(), phone.getCitycode(), phone.getCountrycode())],
                user.getCreated(),
                updatedUser.getLastLogin(),
                "nuevo.token.jwt",
                user.getIsActive()
        )

        when: 'se intenta iniciar sesión'
        def resultado = loginService.login(authHeader).block()

        then: 'debe procesar correctamente el inicio de sesión'
        1 * jwtService.validateTokenAndGetEmail(token) >> email
        1 * userRepository.findByEmail(email) >> Mono.just(user)
        1 * jwtService.generateToken(_) >> "nuevo.token.jwt"
        1 * userRepository.save(_) >> Mono.just(updatedUser)
        1 * phoneRepository.findByUserId(_) >> Flux.just(phone)
        1 * userMapper.toDTO(_) >> responseDTO

        resultado.getStatusCode() == HttpStatus.OK
        resultado.getBody() == responseDTO
    }

    def "debería retornar unauthorized cuando el header de autorización es inválido"() {
        given: 'un header de autorización inválido'
        def authHeader = "InvalidHeader"

        when: 'se intenta iniciar sesión'
        def resultado = loginService.login(authHeader).block()

        then: 'debe retornar unauthorized'
        0 * jwtService.validateTokenAndGetEmail(_)
        resultado.getStatusCode() == HttpStatus.UNAUTHORIZED
        resultado.getBody() == null
    }

    def "debería retornar unauthorized cuando el header es null"() {
        given: 'un header null'
        def authHeader = null

        when: 'se intenta iniciar sesión'
        def resultado = loginService.login(authHeader).block()

        then: 'debe retornar unauthorized'
        0 * jwtService.validateTokenAndGetEmail(_)
        resultado.getStatusCode() == HttpStatus.UNAUTHORIZED
        resultado.getBody() == null
    }

    def "debería retornar unauthorized cuando el token es inválido"() {
        given: 'un token inválido'
        def authHeader = "Bearer token.invalido"

        when: 'se intenta iniciar sesión'
        def resultado = loginService.login(authHeader).block()

        then: 'debe retornar unauthorized'
        1 * jwtService.validateTokenAndGetEmail("token.invalido") >> { throw new RuntimeException("Token inválido") }
        resultado.getStatusCode() == HttpStatus.UNAUTHORIZED
        resultado.getBody() == null
    }

    def "debería retornar unauthorized cuando el usuario no existe"() {
        given: 'un token válido pero usuario inexistente'
        def authHeader = "Bearer token.valido"
        def email = "noexiste@ejemplo.com"

        when: 'se intenta iniciar sesión'
        def resultado = loginService.login(authHeader).block()

        then: 'debe retornar unauthorized'
        1 * jwtService.validateTokenAndGetEmail("token.valido") >> email
        1 * userRepository.findByEmail(email) >> Mono.empty()
        resultado.getStatusCode() == HttpStatus.UNAUTHORIZED
        resultado.getBody() == null
    }

    def "debería iniciar sesión exitosamente con credenciales válidas"() {
        given: 'credenciales válidas'
        def request = new LoginRequestDTO("juan@ejemplo.com", "Password1a2")

        def user = new User()
        user.setId(UUID.randomUUID())
        user.setName("Juan Pérez")
        user.setEmail("juan@ejemplo.com")
        user.setPassword("passwordEncriptado")
        user.setCreated(LocalDateTime.now())
        user.setIsActive(true)

        def updatedUser = new User()
        updatedUser.setId(user.getId())
        updatedUser.setName(user.getName())
        updatedUser.setEmail(user.getEmail())
        updatedUser.setPassword(user.getPassword())
        updatedUser.setCreated(user.getCreated())
        updatedUser.setLastLogin(LocalDateTime.now())
        updatedUser.setToken("nuevo.token.jwt")
        updatedUser.setIsActive(true)

        def phone = new Phone()
        phone.setNumber("987654321")
        phone.setCitycode(2)
        phone.setCountrycode("58")

        def responseDTO = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                [new PhoneResponseDTO(phone.getNumber(), phone.getCitycode(), phone.getCountrycode())],
                user.getCreated(),
                updatedUser.getLastLogin(),
                "nuevo.token.jwt",
                user.getIsActive()
        )

        when: 'se intenta iniciar sesión con credenciales'
        def resultado = loginService.loginWithCredentials(request).block()

        then: 'debe procesar correctamente el inicio de sesión'
        1 * userRepository.findByEmail(request.getEmail()) >> Mono.just(user)
        1 * passwordEncoder.matches(request.getPassword(), user.getPassword()) >> true
        1 * jwtService.generateToken(_) >> "nuevo.token.jwt"
        1 * userRepository.save(_) >> Mono.just(updatedUser)
        1 * phoneRepository.findByUserId(_) >> Flux.just(phone)
        1 * userMapper.toDTO(_) >> responseDTO

        resultado.getStatusCode() == HttpStatus.OK
        resultado.getBody() == responseDTO
    }

    def "debería retornar not found cuando el usuario no existe en login con credenciales"() {
        given: 'un email que no existe'
        def request = new LoginRequestDTO("noexiste@ejemplo.com", "Password1a2")

        when: 'se intenta iniciar sesión'
        def resultado = loginService.loginWithCredentials(request).block()

        then: 'debe retornar not found'
        1 * userRepository.findByEmail(request.getEmail()) >> Mono.empty()
        0 * passwordEncoder.matches(_, _)
        resultado.getStatusCode() == HttpStatus.NOT_FOUND
        resultado.getBody() == null
    }

    def "debería retornar bad request cuando la contraseña es incorrecta"() {
        given: 'un usuario con contraseña incorrecta'
        def request = new LoginRequestDTO("juan@ejemplo.com", "contraseñaIncorrecta")

        def user = new User()
        user.setId(UUID.randomUUID())
        user.setName("Juan Pérez")
        user.setEmail("juan@ejemplo.com")
        user.setPassword("passwordEncriptado")
        user.setIsActive(true)

        when: 'se intenta iniciar sesión'
        def resultado = loginService.loginWithCredentials(request).block()

        then: 'debe retornar bad request'
        1 * userRepository.findByEmail(request.getEmail()) >> Mono.just(user)
        1 * passwordEncoder.matches(request.getPassword(), user.getPassword()) >> false
        0 * jwtService.generateToken(_)
        resultado.getStatusCode() == HttpStatus.BAD_REQUEST
        resultado.getBody() == null
    }

    def "debería retornar bad request cuando el usuario está desactivado"() {
        given: 'un usuario desactivado'
        def request = new LoginRequestDTO("juan@ejemplo.com", "Password1a2")

        def user = new User()
        user.setId(UUID.randomUUID())
        user.setName("Juan Pérez")
        user.setEmail("juan@ejemplo.com")
        user.setPassword("passwordEncriptado")
        user.setIsActive(false)

        when: 'se intenta iniciar sesión'
        def resultado = loginService.loginWithCredentials(request).block()

        then: 'debe retornar bad request'
        1 * userRepository.findByEmail(request.getEmail()) >> Mono.just(user)
        0 * passwordEncoder.matches(_, _)
        resultado.getStatusCode() == HttpStatus.BAD_REQUEST
        resultado.getBody() == null
    }

    def "debería iniciar sesión sin teléfonos cuando el usuario no tiene teléfonos"() {
        given: 'credenciales válidas de usuario sin teléfonos'
        def request = new LoginRequestDTO("juan@ejemplo.com", "Password1a2")

        def user = new User()
        user.setId(UUID.randomUUID())
        user.setName("Juan Pérez")
        user.setEmail("juan@ejemplo.com")
        user.setPassword("passwordEncriptado")
        user.setCreated(LocalDateTime.now())
        user.setIsActive(true)

        def updatedUser = new User()
        updatedUser.setId(user.getId())
        updatedUser.setName(user.getName())
        updatedUser.setEmail(user.getEmail())
        updatedUser.setPassword(user.getPassword())
        updatedUser.setCreated(user.getCreated())
        updatedUser.setLastLogin(LocalDateTime.now())
        updatedUser.setToken("nuevo.token.jwt")
        updatedUser.setIsActive(true)

        def responseDTO = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                [],
                user.getCreated(),
                updatedUser.getLastLogin(),
                "nuevo.token.jwt",
                user.getIsActive()
        )

        when: 'se intenta iniciar sesión con credenciales'
        def resultado = loginService.loginWithCredentials(request).block()

        then: 'debe procesar correctamente el inicio de sesión sin teléfonos'
        1 * userRepository.findByEmail(request.getEmail()) >> Mono.just(user)
        1 * passwordEncoder.matches(request.getPassword(), user.getPassword()) >> true
        1 * jwtService.generateToken(_) >> "nuevo.token.jwt"
        1 * userRepository.save(_) >> Mono.just(updatedUser)
        1 * phoneRepository.findByUserId(_) >> Flux.empty()
        1 * userMapper.toDTO(_) >> responseDTO

        resultado.getStatusCode() == HttpStatus.OK
        resultado.getBody() == responseDTO
    }
}