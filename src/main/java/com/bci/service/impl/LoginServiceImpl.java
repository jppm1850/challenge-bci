package com.bci.service.impl;

import com.bci.entity.Phone;
import com.bci.entity.User;
import com.bci.exception.UserExistsException;
import com.bci.exception.UserNotFoundException;
import com.bci.exception.ValidationException;
import com.bci.mapper.UserMapper;
import com.bci.model.ErrorResponseDTO;
import com.bci.model.LoginRequestDTO;
import com.bci.model.UserResponseDTO;
import com.bci.model.UserSignUpRequestDTO;
import com.bci.repository.PhoneRepository;
import com.bci.repository.UserRepository;
import com.bci.service.JwtService;
import com.bci.service.LoginService;
import com.bci.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    public Mono<ResponseEntity<Object>> login(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("Authorization header required");
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body((Object) errorResponse));
        }

        String token = authHeader.substring(7);
        return processLogin(token)
                .map(userResponse -> ResponseEntity.ok((Object) userResponse))
                .onErrorResume(e -> {
                    log.error("Error during login: {}", e.getMessage());
                    ErrorResponseDTO errorResponse = new ErrorResponseDTO(e.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body((Object) errorResponse));
                });
    }

    private Mono<UserResponseDTO> processLogin(String token) {
        return Mono.fromCallable(() -> jwtService.validateTokenAndGetEmail(token))
                .flatMap(email -> userRepository.findByEmail(email))
                .flatMap(user -> {
                    user.setLastLogin(LocalDateTime.now());
                    user.setToken(jwtService.generateToken(user));

                    return userRepository.save(user)
                            .flatMap(savedUser -> phoneRepository.findByUserId(savedUser.getId())
                                    .collectList()
                                    .map(phones -> {
                                        savedUser.setPhones(phones);
                                        return userMapper.toDTO(savedUser);
                                    })
                            );
                })
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found")));
    }

    @Override
    public Mono<ResponseEntity<Object>> loginWithCredentials(LoginRequestDTO request) {
        return processLoginWithCredentials(request)
                .map(userResponse -> ResponseEntity.ok((Object) userResponse))
                .onErrorResume(e -> {
                    log.error("Error during credentials login: {}", e.getMessage());

                    HttpStatus status = HttpStatus.UNAUTHORIZED;
                    if (e instanceof UserNotFoundException) {
                        status = HttpStatus.NOT_FOUND;
                    } else if (e instanceof ValidationException) {
                        status = HttpStatus.BAD_REQUEST;
                    }

                    ErrorResponseDTO errorResponse = new ErrorResponseDTO(e.getMessage());
                    return Mono.just(ResponseEntity
                            .status(status)
                            .body((Object) errorResponse));
                });
    }

    private Mono<UserResponseDTO> processLoginWithCredentials(LoginRequestDTO request) {
        return userRepository.findByEmail(request.getEmail())
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + request.getEmail())))
                .flatMap(user -> {
                    if (!user.getIsActive()) {
                        return Mono.error(new ValidationException("User account is disabled"));
                    }

                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return Mono.error(new ValidationException("Invalid password"));
                    }

                    String newToken = jwtService.generateToken(user);
                    user.setToken(newToken);
                    user.setLastLogin(LocalDateTime.now());

                    return userRepository.save(user)
                            .flatMap(savedUser -> phoneRepository.findByUserId(savedUser.getId())
                                    .collectList()
                                    .map(phones -> {
                                        savedUser.setPhones(phones);
                                        return userMapper.toDTO(savedUser);
                                    })
                            );
                });
    }
}