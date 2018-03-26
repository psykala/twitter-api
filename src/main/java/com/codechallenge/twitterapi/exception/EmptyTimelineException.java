package com.codechallenge.twitterapi.exception;

public class EmptyTimelineException extends RuntimeException {
    private final String message;

    public EmptyTimelineException(String userName) {
        message = "The user [" + userName + "] does not follow any other users";
    }

    public String getMessage() {
        return message;
    }
}
