package com.codechallenge.twitterapi.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.codechallenge.twitterapi.dto.UserDTO;
import com.codechallenge.twitterapi.model.User;

public class UserPojoToDtoConverter {
    public static UserDTO convert(User user) {
        return new UserDTO(user.getName());
    }

    public static List<UserDTO> convert(List<User> users) {
        return users.stream()
                .map(UserPojoToDtoConverter::convert)
                .collect(Collectors.toList());
    }
}
