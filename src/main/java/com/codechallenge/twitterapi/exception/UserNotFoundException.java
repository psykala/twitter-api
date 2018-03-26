package com.codechallenge.twitterapi.exception;

public class UserNotFoundException extends RuntimeException {
    private final String message;

    public UserNotFoundException(String userName) {
        message = "User [" + userName + "] not found";
    }

    public String getMessage() {
        return message;
    }
}
