package com.bci.reto.service

import com.bci.entity.Phone
import com.bci.entity.User
import com.bci.exception.UserExistsException
import com.bci.mapper.UserMapper
import com.bci.model.PhoneRequestDTO
import com.bci.model.PhoneResponseDTO
import com.bci.model.UserResponseDTO
import com.bci.model.UserSignUpRequestDTO
import com.bci.repository.PhoneRepository
import com.bci.repository.UserRepository
import com.bci.service.JwtService
import com.bci.service.UserService
import com.bci.service.impl.UserServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import spock.lang.Specification

import java.time.LocalDateTime

class UserServiceSpec extends Specification {
    UserRepository userRepository
    PhoneRepository phoneRepository
    PasswordEncoder passwordEncoder
    JwtService jwtService
    UserMapper userMapper
    UserService userService

    def setup() {
        userRepository = Mock()
        phoneRepository = Mock()
        passwordEncoder = Mock()
        jwtService = Mock()
        userMapper = Mock()
        userService = new UserServiceImpl(userRepository, phoneRepository, passwordEncoder, jwtService, userMapper)
    }

    def "debería registrar un nuevo usuario exitosamente con teléfonos"() {
        given: 'una solicitud de registro válida con teléfonos'
        def phoneDto = new PhoneRequestDTO("123456789", 1, "57")
        def request = new UserSignUpRequestDTO("Juan Pérez", "juan@ejemplo.com", "Password1a2", [phoneDto])

        def user = new User()
        user.setName("Juan Pérez")
        user.setEmail("juan@ejemplo.com")
        user.setPassword("Password1a2")

        def savedUser = new User()
        savedUser.setId(UUID.randomUUID())
        savedUser.setName("Juan Pérez")
        savedUser.setEmail("juan@ejemplo.com")
        savedUser.setPassword("contraseñaEncriptada")
        savedUser.setCreated(LocalDateTime.now())
        savedUser.setIsActive(true)

        def userWithToken = new User()
        userWithToken.setId(savedUser.getId())
        userWithToken.setName("Juan Pérez")
        userWithToken.setEmail("juan@ejemplo.com")
        userWithToken.setPassword("contraseñaEncriptada")
        userWithToken.setCreated(savedUser.getCreated())
        userWithToken.setToken("jwt.token")
        userWithToken.setIsActive(true)

        def phone = new Phone()
        phone.setNumber("123456789")
        phone.setCitycode(1)
        phone.setCountrycode("57")
        phone.setUserId(savedUser.getId())

        def responseDTO = new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                [new PhoneResponseDTO(phone.getNumber(), phone.getCitycode(), phone.getCountrycode())],
                savedUser.getCreated(),
                null,
                "jwt.token",
                savedUser.getIsActive()
        )

        when: 'se intenta registrar el usuario'
        def resultado = userService.signUp(request).block()

        then: 'se debe procesar correctamente la solicitud'
        1 * userRepository.findByEmail(request.getEmail()) >> Mono.empty()
        1 * userMapper.toEntity(request) >> user
        1 * passwordEncoder.encode(request.getPassword()) >> "contraseñaEncriptada"
        1 * userRepository.save(_) >> Mono.just(savedUser)
        1 * jwtService.generateToken(_) >> "jwt.token"
        1 * userRepository.save(_) >> Mono.just(userWithToken)
        1 * userMapper.phoneRequestDTOsToEntities(_) >> [phone]
        1 * phoneRepository.saveAll(_) >> Flux.just(phone)
        1 * userMapper.toDTO(_) >> responseDTO

        resultado.getStatusCode() == HttpStatus.CREATED
        resultado.getBody() == responseDTO
    }

    def "debería registrar usuario sin teléfonos exitosamente"() {
        given: 'una solicitud sin teléfonos'
        def request = new UserSignUpRequestDTO("Juan Pérez", "juan@ejemplo.com", "Password1a2", [])

        def user = new User()
        user.setName("Juan Pérez")
        user.setEmail("juan@ejemplo.com")
        user.setPassword("Password1a2")

        def savedUser = new User()
        savedUser.setId(UUID.randomUUID())
        savedUser.setName("Juan Pérez")
        savedUser.setEmail("juan@ejemplo.com")
        savedUser.setPassword("contraseñaEncriptada")
        savedUser.setCreated(LocalDateTime.now())
        savedUser.setIsActive(true)

        def userWithToken = new User()
        userWithToken.setId(savedUser.getId())
        userWithToken.setName("Juan Pérez")
        userWithToken.setEmail("juan@ejemplo.com")
        userWithToken.setPassword("contraseñaEncriptada")
        userWithToken.setCreated(savedUser.getCreated())
        userWithToken.setToken("jwt.token")
        userWithToken.setIsActive(true)

        def responseDTO = new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                [],
                savedUser.getCreated(),
                null, // lastLogin
                "jwt.token",
                savedUser.getIsActive()
        )

        when: 'se intenta registrar el usuario'
        def resultado = userService.signUp(request).block()

        then: 'se debe procesar correctamente sin teléfonos'
        1 * userRepository.findByEmail(request.getEmail()) >> Mono.empty()
        1 * userMapper.toEntity(request) >> user
        1 * passwordEncoder.encode(request.getPassword()) >> "contraseñaEncriptada"
        1 * userRepository.save(_) >> Mono.just(savedUser)
        1 * jwtService.generateToken(_) >> "jwt.token"
        1 * userRepository.save(_) >> Mono.just(userWithToken)
        1 * userMapper.toDTO(_) >> responseDTO
        0 * phoneRepository.saveAll(_)
        0 * userMapper.phoneRequestDTOsToEntities(_)

        resultado.getStatusCode() == HttpStatus.CREATED
        resultado.getBody() == responseDTO
    }

    def "debería registrar usuario con lista de teléfonos null"() {
        given: 'una solicitud con teléfonos null'
        def request = new UserSignUpRequestDTO("Juan Pérez", "juan@ejemplo.com", "Password1a2", null)

        def user = new User()
        user.setName("Juan Pérez")
        user.setEmail("juan@ejemplo.com")
        user.setPassword("Password1a2")

        def savedUser = new User()
        savedUser.setId(UUID.randomUUID())
        savedUser.setName("Juan Pérez")
        savedUser.setEmail("juan@ejemplo.com")
        savedUser.setPassword("contraseñaEncriptada")
        savedUser.setCreated(LocalDateTime.now())
        savedUser.setIsActive(true)

        def userWithToken = new User()
        userWithToken.setId(savedUser.getId())
        userWithToken.setName("Juan Pérez")
        userWithToken.setEmail("juan@ejemplo.com")
        userWithToken.setPassword("contraseñaEncriptada")
        userWithToken.setCreated(savedUser.getCreated())
        userWithToken.setToken("jwt.token")
        userWithToken.setIsActive(true)

        def responseDTO = new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                [],
                savedUser.getCreated(),
                null,
                "jwt.token",
                savedUser.getIsActive()
        )

        when: 'se intenta registrar el usuario'
        def resultado = userService.signUp(request).block()

        then: 'se debe procesar correctamente sin teléfonos'
        1 * userRepository.findByEmail(request.getEmail()) >> Mono.empty()
        1 * userMapper.toEntity(request) >> user
        1 * passwordEncoder.encode(request.getPassword()) >> "contraseñaEncriptada"
        1 * userRepository.save(_) >> Mono.just(savedUser)
        1 * jwtService.generateToken(_) >> "jwt.token"
        1 * userRepository.save(_) >> Mono.just(userWithToken)
        1 * userMapper.toDTO(_) >> responseDTO
        0 * phoneRepository.saveAll(_)

        resultado.getStatusCode() == HttpStatus.CREATED
        resultado.getBody() == responseDTO
    }

    def "debería devolver conflicto cuando el email ya existe"() {
        given: 'una solicitud con email ya registrado'
        def request = new UserSignUpRequestDTO("Juan Pérez", "existente@ejemplo.com", "Password1a2", [])
        def usuarioExistente = new User()
        usuarioExistente.setEmail(request.getEmail())

        when: 'se intenta registrar el usuario'
        def resultado = userService.signUp(request).block()

        then: 'debe devolver una respuesta de conflicto'
        1 * userRepository.findByEmail(request.getEmail()) >> Mono.just(usuarioExistente)
        0 * userMapper.toEntity(_)
        0 * passwordEncoder.encode(_)
        0 * userRepository.save(_)

        resultado.getStatusCode() == HttpStatus.CONFLICT
        resultado.getBody().getError()[0].getDetail().contains("Email already registered")
        resultado.getBody().getError()[0].getCodigo() == HttpStatus.CONFLICT.value()
    }

    def "debería manejar errores inesperados durante el registro"() {
        given: 'una solicitud válida pero ocurre un error inesperado'
        def request = new UserSignUpRequestDTO("Juan Pérez", "juan@ejemplo.com", "Password1a2", [])

        when: 'se intenta registrar el usuario'
        def resultado = userService.signUp(request).block()

        then: 'debe devolver una respuesta de error'
        1 * userRepository.findByEmail(request.getEmail()) >> Mono.empty()
        1 * userMapper.toEntity(request) >> { throw new RuntimeException("Error inesperado") }

        resultado.getStatusCode() == HttpStatus.BAD_REQUEST
        resultado.getBody().getError()[0].getDetail() == "Error inesperado"
        resultado.getBody().getError()[0].getCodigo() == HttpStatus.BAD_REQUEST.value()
    }
}