package com.gnemirko.bank_rest.exception;

public class UserNameAlreadyExistsException extends RuntimeException {
    public UserNameAlreadyExistsException(String username) {
        super("Username already exists: " + username);
    }
}
