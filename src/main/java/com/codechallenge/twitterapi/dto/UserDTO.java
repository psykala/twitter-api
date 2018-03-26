package com.codechallenge.twitterapi.dto;

import javax.validation.constraints.NotEmpty;

public class UserDTO {
    @NotEmpty
    private String name;

    private UserDTO() {
    }

    public UserDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
