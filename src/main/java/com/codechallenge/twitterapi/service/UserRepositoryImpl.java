package com.codechallenge.twitterapi.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.codechallenge.twitterapi.model.User;

@Component
public class UserRepositoryImpl implements UserRepository {
    private Map<String, User> allUsers = new HashMap<>();

    @Override
    public User save(User user) {
        allUsers.put(user.getName()
                .toLowerCase(), user);
        return user;
    }

    @Override
    public Optional<User> findByName(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return Optional.empty();
        }
        return getUser(userName);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(allUsers.values());
    }

    @Override
    public List<User> findAllFollowedUsers(String userName) {
        Optional<User> user = getUser(userName);
        return user.isPresent() ? user.get()
                .getFollowedUsers() : Collections.emptyList();
    }

    private Optional<User> getUser(String userName) {
        if (StringUtils.isEmpty(userName)) {
            Optional.empty();
        }
        return Optional.ofNullable(allUsers.get(userName.toLowerCase()));
    }
}
