package com.codechallenge.twitterapi.dto;

public class ApiErrorDTO {
    private final int code;

    private final String message;

    public ApiErrorDTO(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
