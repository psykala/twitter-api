package com.codechallenge.twitterapi.service;

import java.util.List;

import com.codechallenge.twitterapi.dto.PostDTO;
import com.codechallenge.twitterapi.dto.UserDTO;

public interface PostService {
    PostDTO addNewPost(String text, String userName);

    List<PostDTO> findByUserName(String userName);

    List<PostDTO> findPostsByUsers(List<UserDTO> users);
}
