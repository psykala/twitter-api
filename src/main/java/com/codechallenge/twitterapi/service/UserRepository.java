package com.codechallenge.twitterapi.service;

import java.util.List;
import java.util.Optional;

import com.codechallenge.twitterapi.model.User;

public interface UserRepository {

    User save(User user);

    Optional<User> findByName(String userName);

    List<User> findAll();

    List<User> findAllFollowedUsers(String userName);

    default boolean userExists(String userName) {
        return findByName(userName).isPresent();
    }
}
