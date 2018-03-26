package com.codechallenge.twitterapi.model;

import java.time.LocalDateTime;

public class Post {
    private String text;

    private User user;

    private LocalDateTime dateTime;

    public Post(String text, User user, LocalDateTime dateTime) {
        this.text = text;
        this.user = user;
        this.dateTime = dateTime;
    }

    public String getText() {
        return text;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
