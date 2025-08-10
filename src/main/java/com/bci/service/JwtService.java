package com.bci.service;


import com.bci.entity.User;

public interface JwtService {

    String generateToken(User user);
    String validateTokenAndGetEmail(String token);
}
