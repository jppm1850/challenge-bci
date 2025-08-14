package com.bci.service;


import com.bci.model.LoginRequestDTO;
import com.bci.model.UserResponseDTO;
import com.bci.model.UserSignUpRequestDTO;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface LoginService {

    Mono<ResponseEntity<Object>> login(String authHeader);
    Mono<ResponseEntity<Object>> loginWithCredentials(LoginRequestDTO request);
}
