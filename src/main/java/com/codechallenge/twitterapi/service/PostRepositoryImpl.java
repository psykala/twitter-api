package com.codechallenge.twitterapi.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.codechallenge.twitterapi.model.Post;
import com.codechallenge.twitterapi.model.User;

@Component
public class PostRepositoryImpl implements PostRepository {
    private Map<String, List<Post>> allPosts = new HashMap<>();

    @Override
    public Post save(Post post) {
        List<Post> userPosts = getAllUserPosts(post.getUser());
        userPosts.add(post);
        return getLatestPost(userPosts);
    }

    @Override
    public List<Post> findByUserName(String userName) {
        List<Post> userPosts = allPosts.get(userName.toLowerCase());

        return userPosts != null ? userPosts : Collections.emptyList();
    }

    private List<Post> getAllUserPosts(User user) {
        List<Post> userPosts = allPosts.get(user.getName()
                .toLowerCase());
        if (userPosts == null) {
            userPosts = new ArrayList<>();
            allPosts.put(user.getName()
                    .toLowerCase(), userPosts);
        }
        return userPosts;
    }

    private Post getLatestPost(List<Post> userPosts) {
        return userPosts.get(userPosts.size() - 1);
    }
}
