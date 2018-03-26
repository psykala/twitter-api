package com.codechallenge.twitterapi.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class CommentDTO {

    @NotEmpty
    @Size(max = 140)
    private String text;

    private CommentDTO() {
    }

    public CommentDTO(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setComment(String text) {
        this.text = text;
    }
}
