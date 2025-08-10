package com.bci.exception;


public final class UserNotFoundException extends RuntimeException implements UserError {
    public UserNotFoundException(String message) {
        super(message);
    }
}
