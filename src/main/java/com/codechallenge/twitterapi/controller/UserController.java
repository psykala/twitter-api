package com.codechallenge.twitterapi.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.codechallenge.twitterapi.dto.ApiErrorDTO;
import com.codechallenge.twitterapi.dto.CommentDTO;
import com.codechallenge.twitterapi.dto.PostDTO;
import com.codechallenge.twitterapi.dto.UserDTO;
import com.codechallenge.twitterapi.exception.EmptyTimelineException;
import com.codechallenge.twitterapi.exception.FollowingException;
import com.codechallenge.twitterapi.exception.PostNotFoundException;
import com.codechallenge.twitterapi.exception.UserNotFoundException;
import com.codechallenge.twitterapi.model.User;
import com.codechallenge.twitterapi.service.PostService;
import com.codechallenge.twitterapi.service.UserRepository;
import com.codechallenge.twitterapi.utils.UserPojoToDtoConverter;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    private final PostService postService;

    @Autowired
    public UserController(UserRepository userRepository, PostService postService) {
        this.userRepository = userRepository;
        this.postService = postService;
    }

    @GetMapping(produces = APPLICATION_JSON_UTF8_VALUE)
    public List<UserDTO> retrieveAllRegistredUsers() {
        return UserPojoToDtoConverter.convert(userRepository.findAll());
    }

    @PostMapping(consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDto) {
        User newUser = userRepository.save(new User(userDto.getName()));
        UserDTO newUserDto = new UserDTO(newUser.getName());
        HttpHeaders headers = buildResponseHeaders(newUserDto.getName());

        return new ResponseEntity<>(newUserDto, headers, HttpStatus.CREATED);
    }

    @GetMapping(path = "/{userName}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserDTO retrieveUserDetails(@PathVariable String userName) {
        User user = retrieveUserByName(userName);
        return UserPojoToDtoConverter.convert(user);
    }

    @GetMapping(path = "/{userName}/posts", produces = APPLICATION_JSON_UTF8_VALUE)
    public List<PostDTO> retrievePostsByUser(@PathVariable String userName) {
        User user = retrieveUserByName(userName);
        List<PostDTO> posts = postService.findByUserName(user.getName());

        if (posts.isEmpty()) {
            throw new PostNotFoundException(userName);
        }
        return posts;
    }

    @PostMapping(path = "/{userName}/posts", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<PostDTO> addNewPost(@PathVariable String userName, @Valid @RequestBody CommentDTO commentDto) {
        postService.addNewPost(commentDto.getText(), userName);
        return retrievePostsByUser(userName);
    }

    @GetMapping(path = "/{userName}/followed-users", produces = APPLICATION_JSON_VALUE)
    public List<UserDTO> retrieveFollowedUsers(@PathVariable String userName) {
        User user = retrieveUserByName(userName);
        return retrieveFollowedUsers(user);
    }

    @PostMapping(path = "/{userName}/followed-users", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<UserDTO> startFollowingUser(@PathVariable String userName, @RequestBody UserDTO userToFollowDto) {
        User user = retrieveUserByName(userName);
        User userToFollow = retrieveUserByName(userToFollowDto.getName());
        throwExceptionWhenIdentical(user, userToFollow);

        user.follow(userToFollow);
        return UserPojoToDtoConverter.convert(user.getFollowedUsers());
    }

    @GetMapping(path = "/{userName}/timelines", produces = APPLICATION_JSON_VALUE)
    public List<PostDTO> retrievePostsByOtherUsers(@PathVariable String userName) {
        User user = retrieveUserByName(userName);
        return retrievePostsFromFollowedUsers(user);
    }

    @ExceptionHandler({ UserNotFoundException.class, FollowingException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorDTO userNotFound(Exception ex) {
        return new ApiErrorDTO(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler({ PostNotFoundException.class, EmptyTimelineException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorDTO reportNotFoundError(Exception ex) {
        return new ApiErrorDTO(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    private User retrieveUserByName(String userName) {
        Optional<User> user = userRepository.findByName(userName);

        if (!user.isPresent()) {
            throw new UserNotFoundException(userName);
        }
        return user.get();
    }

    private List<PostDTO> retrievePostsFromFollowedUsers(User user) {
        List<UserDTO> followedUsers = retrieveFollowedUsers(user);
        return postService.findPostsByUsers(followedUsers);
    }

    private List<UserDTO> retrieveFollowedUsers(User user) {
        List<User> users = user.getFollowedUsers();

        if (users.isEmpty()) {
            throw new EmptyTimelineException(user.getName());
        }
        return UserPojoToDtoConverter.convert(user.getFollowedUsers());
    }

    private static HttpHeaders buildResponseHeaders(String resourceId) {
        HttpHeaders headers = new HttpHeaders();
        URI locationURI = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{resourceId}")
                .buildAndExpand(resourceId)
                .toUri();
        headers.setLocation(locationURI);

        return headers;
    }

    private static void throwExceptionWhenIdentical(User user, User userToFollow) {
        if (user.equals(userToFollow)) {
            throw new FollowingException(user.getName(), userToFollow.getName());
        }
    }
}
