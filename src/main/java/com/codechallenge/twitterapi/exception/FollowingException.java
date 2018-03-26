package com.codechallenge.twitterapi.exception;

public class FollowingException extends RuntimeException {
    private final String message;

    public FollowingException(String userNameOne, String userNameTwo) {
        message = "User [" + userNameOne + "] cannot follow [" + userNameTwo + "]";
    }

    public String getMessage() {
        return message;
    }
}
