package com.codechallenge.twitterapi.exception;

public class PostNotFoundException extends RuntimeException {
    private final String message;

    public PostNotFoundException(String userName) {
        message = "User [" + userName + "] has not yet published any post";
    }

    public String getMessage() {
        return message;
    }
}
