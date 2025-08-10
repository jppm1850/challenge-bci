package com.bci.service;


import com.bci.model.UserSignUpRequestDTO;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<ResponseEntity<Object>> signUp(UserSignUpRequestDTO request);

}
