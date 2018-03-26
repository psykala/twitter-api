package com.codechallenge.twitterapi.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class User {
    private String name;

    private Set<User> followedUsers = new HashSet<>();

    public void setName(String name) {
        this.name = name;
    }

    private User() {
    }

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void follow(User userToFollow) {
        followedUsers.add(userToFollow);
    }

    public List<User> getFollowedUsers() {
        return followedUsers.stream()
                .collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((followedUsers == null) ? 0 : followedUsers.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (followedUsers == null) {
            if (other.followedUsers != null)
                return false;
        } else if (!followedUsers.equals(other.followedUsers))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
