package com.codechallenge.twitterapi.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.codechallenge.twitterapi.dto.PostDTO;
import com.codechallenge.twitterapi.dto.UserDTO;
import com.codechallenge.twitterapi.model.Post;
import com.codechallenge.twitterapi.model.User;
import com.codechallenge.twitterapi.utils.PostPojo2DtoConverter;

@Service
public class PostServiceImpl implements PostService {

    private PostRepository postRepository;

    private UserRepository userRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PostDTO addNewPost(String text, String userName) {
        User user = addOrGetUser(userName);
        Post post = postRepository.save(new Post(text, user, LocalDateTime.now()));

        return PostPojo2DtoConverter.convert(post);
    }

    @Override
    public List<PostDTO> findByUserName(String userName) {
        List<Post> posts = postRepository.findByUserName(userName);
        sortDescendingByDateTime(posts);

        return PostPojo2DtoConverter.convert(posts);
    }

    @Override
    public List<PostDTO> findPostsByUsers(List<UserDTO> users) {
        List<Post> posts = mergePostsOfAllUsers(users);
        sortDescendingByDateTime(posts);

        return PostPojo2DtoConverter.convert(posts);
    }

    private User addOrGetUser(String userName) {
        Optional<User> user = userRepository.findByName(userName);

        if (!user.isPresent()) {
            user = Optional.of(userRepository.save(new User(userName)));
        }
        return user.get();
    }

    private List<Post> mergePostsOfAllUsers(List<UserDTO> users) {
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }

        List<Post> result = new LinkedList<>();
        for (UserDTO user : users) {
            result.addAll(postRepository.findByUserName(user.getName()));
        }
        return result;
    }

    private static void sortDescendingByDateTime(List<Post> posts) {
        Collections.sort(posts, (post1, post2) -> post2.getDateTime()
                .compareTo(post1.getDateTime()));
    }
}
