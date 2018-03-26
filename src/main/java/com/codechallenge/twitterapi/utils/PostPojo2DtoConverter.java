package com.codechallenge.twitterapi.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.codechallenge.twitterapi.dto.PostDTO;
import com.codechallenge.twitterapi.model.Post;

public class PostPojo2DtoConverter {
    public static PostDTO convert(Post post) {
        return new PostDTO(post.getText(), post.getUser()
                .getName(), post.getDateTime());
    }

    public static List<PostDTO> convert(List<Post> posts) {
        return posts.stream()
                .map(PostPojo2DtoConverter::convert)
                .collect(Collectors.toList());
    }
}
