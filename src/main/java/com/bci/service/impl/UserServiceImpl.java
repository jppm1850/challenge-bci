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
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    public Mono<ResponseEntity<Object>> signUp(UserSignUpRequestDTO request) {
        return processSignUp(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body((Object) response))
                .onErrorResume(e -> {
                    log.error("Error in signup: ", e);

                    HttpStatus status;
                    if (e instanceof UserExistsException) {
                        status = HttpStatus.CONFLICT;
                    } else {
                        status = HttpStatus.BAD_REQUEST;
                    }

                    ErrorResponseDTO errorResponse = new ErrorResponseDTO(e.getMessage());
                    return Mono.just(ResponseEntity.status(status).body((Object) errorResponse));
                });
    }

    private Mono<UserResponseDTO> processSignUp(UserSignUpRequestDTO request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(existingUser -> Mono.<UserResponseDTO>error(
                        new UserExistsException("Email already registered")))
                .switchIfEmpty(Mono.defer(() -> {
                    User user = userMapper.toEntity(request);
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setCreated(LocalDateTime.now());
                    user.setIsActive(true);

                    return userRepository.save(user)
                            .flatMap(savedUser -> {
                                String token = jwtService.generateToken(savedUser);
                                savedUser.setToken(token);

                                return userRepository.save(savedUser)
                                        .flatMap(userWithToken -> {
                                            if (request.getPhones() == null || request.getPhones().isEmpty()) {
                                                return Mono.just(userMapper.toDTO(userWithToken));
                                            }

                                            List<Phone> phones = userMapper.phoneRequestDTOsToEntities(request.getPhones());

                                            phones = phones.stream()
                                                    .filter(phone -> phone != null)
                                                    .peek(phone -> phone.setUserId(userWithToken.getId()))
                                                    .collect(Collectors.toList());

                                            if (phones.isEmpty()) {
                                                return Mono.just(userMapper.toDTO(userWithToken));
                                            }

                                            return phoneRepository.saveAll(phones)
                                                    .collectList()
                                                    .map(savedPhones -> {
                                                        userWithToken.setPhones(savedPhones);
                                                        return userMapper.toDTO(userWithToken);
                                                    });
                                        });
                            });
                }));
    }


}