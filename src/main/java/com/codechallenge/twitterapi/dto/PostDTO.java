package com.codechallenge.twitterapi.dto;

import java.time.LocalDateTime;

public class PostDTO {
    private String text;

    private String userName;

    private LocalDateTime dateTime;

    private PostDTO() {
    }

    public PostDTO(String text, String userName, LocalDateTime dateTime) {
        this.text = text;
        this.userName = userName;
        this.dateTime = dateTime;
    }

    public String getText() {
        return text;
    }

    public String getUserName() {
        return userName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
