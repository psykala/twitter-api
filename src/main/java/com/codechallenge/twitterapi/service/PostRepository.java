package com.codechallenge.twitterapi.service;

import java.util.List;

import com.codechallenge.twitterapi.model.Post;
import com.codechallenge.twitterapi.model.User;

public interface PostRepository {
    Post save(Post post);

    List<Post> findByUserName(String userName);
}
