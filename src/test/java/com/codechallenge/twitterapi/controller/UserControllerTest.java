package com.codechallenge.twitterapi.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.codechallenge.twitterapi.dto.CommentDTO;
import com.codechallenge.twitterapi.dto.PostDTO;
import com.codechallenge.twitterapi.dto.UserDTO;
import com.codechallenge.twitterapi.model.User;
import com.codechallenge.twitterapi.service.PostService;
import com.codechallenge.twitterapi.service.UserRepository;
import com.codechallenge.twitterapi.utils.UserPojoToDtoConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(UserController.class)
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PostService postService;

    @Test
    public void shouldReturnEmptyListWhenNoGivenUser() throws Exception {
        // given
        List<UserDTO> emptyList = Collections.emptyList();

        // when-then
        mockMvc.perform(get("/users").contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().bytes(convertObjectToJson(emptyList)));
    }

    @Test
    public void shouldReturnAllRegisteredUsers() throws Exception {
        // given
        List<User> allUsers = Arrays.asList(new User("FirstUser"), new User("SecondUser"), new User("ThirdUser"));
        List<UserDTO> allUsersDto = UserPojoToDtoConverter.convert(allUsers);

        when(userRepository.findAll()).thenReturn(allUsers);

        // when-then
        mockMvc.perform(get("/twitter/api/users").contextPath("/twitter/api")
                .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().bytes(convertObjectToJson(allUsersDto)))
                .andDo(document("users/list", preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[].name").description("The name of the user"))));
    }

    @Test
    public void shouldCreateAndReturnNewUser() throws Exception {
        // given
        String userName = "User";
        UserDTO newUserDto = new UserDTO(userName);
        User newUser = Mockito.mock(User.class);

        when(newUser.getName()).thenReturn(userName);
        when(userRepository.save(Mockito.any(User.class))).thenReturn(newUser);

        // when-then
        mockMvc.perform(post("/twitter/api/users").contextPath("/twitter/api")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(newUserDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().bytes(convertObjectToJson(newUserDto)))
                .andDo(document("users/create", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestFields(fieldWithPath(".name").description("The name of the user to be created")),
                        responseFields(fieldWithPath(".name").description("The name of the user"))));
    }

    @Test
    public void shouldReturnUserDetails() throws Exception {
        // given
        String userName = "User";
        User user = new User(userName);
        UserDTO userDto = new UserDTO(userName);

        when(userRepository.findByName(userDto.getName())).thenReturn(Optional.of(user));

        // when-then
        mockMvc.perform(get("/twitter/api/users/" + userName).contextPath("/twitter/api")
                .contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().bytes(convertObjectToJson(userDto)))
                .andDo(document("users/details", preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath(".name").description("The name of the user"))));
    }

    @Test
    public void shouldReturnBadRequestWhenGettingNonExistingUserDetails() throws Exception {
        // given
        User user = new User("NonExistingUser");

        when(userRepository.findByName(user.getName())).thenReturn(Optional.empty());

        // when-then
        mockMvc.perform(get("/users/" + user.getName()).contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("User [" + user.getName() + "] not found")));
    }

    @Test
    public void shouldReturnAllPostsPublishedByUser() throws Exception {
        // given
        User user = new User("User");
        PostDTO post1 = createPost("post one", user);
        PostDTO post2 = createPost("post two", user);
        List<PostDTO> posts = Arrays.asList(post1, post2);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(postService.findByUserName(user.getName())).thenReturn(posts);

        // when-then
        mockMvc.perform(get("/twitter/api/users/" + user.getName() + "/posts").contextPath("/twitter/api")
                .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].text", is("post one")))
                .andExpect(jsonPath("$[1].text", is("post two")))
                .andDo(document("posts/list", preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[].text").description("The text of the published post"),
                                fieldWithPath("[].userName").description("The author of the post"),
                                fieldWithPath("[].dateTime").description("The date of publishing the post"))));
    }

    @Test
    public void shouldReturnNotFoundWhenNoPostsPublishedByUser() throws Exception {
        // given
        User user = new User("User");

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(postService.findByUserName(user.getName())).thenReturn(Collections.emptyList());

        // when-then
        mockMvc.perform(get("/users/" + user.getName() + "/posts").contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .string(containsString("User [" + user.getName() + "] has not yet published any post")));
    }

    @Test
    public void shouldCreateNewPostAndReturnAllUserPosts() throws Exception {
        // given
        User user = new User("User");
        CommentDTO commentDto = new CommentDTO("a new post to be added");
        PostDTO existingPost = createPost("existing post", user);
        PostDTO newPost = createPost(commentDto.getText(), user);

        when(postService.addNewPost(commentDto.getText(), user.getName())).thenReturn(mock(PostDTO.class));
        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(postService.findByUserName(user.getName())).thenReturn(Arrays.asList(newPost, existingPost));

        // when-then
        mockMvc.perform(post("/twitter/api/users/" + user.getName() + "/posts").contextPath("/twitter/api")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(commentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].text", is(commentDto.getText())))
                .andExpect(jsonPath("$[1].text", is(existingPost.getText())))
                .andDo(document("posts/create", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestFields(fieldWithPath(".text")
                                .description("The text to publish. " + getConstraints(CommentDTO.class, "text"))),
                        responseFields(fieldWithPath("[].text").description("The text of the published post. "),
                                fieldWithPath("[].userName").description("The author of the post"),
                                fieldWithPath("[].dateTime").description("The date of publishing the post"))));

    }

    @Test
    public void shouldReturnBadRequestWhenPostMessageIsTooLong() throws Exception {
        // given
        User user = new User("User");
        CommentDTO tooLongPostMessage = new CommentDTO(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean mollis tellus sed nisl convallis tincidunt. Praesent at lorem ex. Nam tristique.");
        PostDTO existingPost = createPost("existing post", user);
        PostDTO newPost = createPost(tooLongPostMessage.getText(), user);

        when(postService.addNewPost(tooLongPostMessage.getText(), user.getName())).thenReturn(newPost);
        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(postService.findByUserName(user.getName())).thenReturn(Arrays.asList(existingPost));

        // when-then
        mockMvc.perform(post("/users/" + user.getName() + "/posts").contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(tooLongPostMessage)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnNotFoundWhenNoFollowedUsers() throws Exception {
        // given
        User user = new User("User");

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

        // when-then
        mockMvc.perform(get("/users/" + user.getName() + "/followed-users").contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnAllOtherUsersFollowedByGivenUser() throws Exception {
        // given
        User user = mock(User.class);

        when(user.getName()).thenReturn("User");
        when(user.getFollowedUsers()).thenReturn(Arrays.asList(new User("Other1"), new User("Other2")));
        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

        // when-then
        mockMvc.perform(get("/users/" + user.getName() + "/followed-users").contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Other1")))
                .andExpect(jsonPath("$[1].name", is("Other2")));
    }

    @Test
    public void shouldAddNewUserToFollowAndReturnAllFollowedUsers() throws Exception {
        // given
        UserDTO userToFollow = new UserDTO("UserToFollow");
        User user = new User("User");
        user.follow(new User("FollowedUser1"));
        user.follow(new User("FollowedUser2"));

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(userRepository.findByName(userToFollow.getName()))
                .thenReturn(Optional.of(new User(userToFollow.getName())));

        // when-then
        mockMvc.perform(post("/twitter/api/users/" + user.getName() + "/followed-users").contextPath("/twitter/api")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(userToFollow)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(3)))
                .andDo(document("users/follow", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestFields(fieldWithPath(".name").description("The name of the user to follow")),
                        responseFields(fieldWithPath("[].name").description("The name of the followed user"))));

    }

    @Test
    public void shouldReturnBadRequestWhenUserWantsToFollowHimself() throws Exception {
        // given
        User user = new User("User");
        UserDTO userToFollow = new UserDTO("User");

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(userRepository.findByName(userToFollow.getName()))
                .thenReturn(Optional.of(new User(userToFollow.getName())));

        // when-then
        mockMvc.perform(post("/users/" + user.getName() + "/followed-users").contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(userToFollow)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnNotFoundWhenUserDoNotFollowOthers() throws Exception {
        // given
        User user = new User("User");

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

        // when-then
        mockMvc.perform(get("/users/" + user.getName() + "/timelines").contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnEmptyArrayWhenNoPostsPublishedByFollowedUsers() throws Exception {
        // given
        User user = new User("User");
        user.follow(new User("OtherUser"));

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(postService.findPostsByUsers(Mockito.anyList())).thenReturn(Collections.emptyList());

        // when-then
        mockMvc.perform(get("/users/" + user.getName() + "/timelines").contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnAllPostsPublishedByOtherUsers() throws Exception {
        // given
        User user = new User("User");
        User otherUserOne = new User("OtherUserOne");
        User otherUserTwo = new User("OtherUserTwo");
        user.follow(otherUserOne);
        user.follow(otherUserTwo);

        List<PostDTO> postsPublishedByOthers = Arrays.asList(createPost("post one", otherUserOne),
                createPost("post two", otherUserTwo));

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(postService.findPostsByUsers(Mockito.anyList())).thenReturn(postsPublishedByOthers);

        // when-then
        mockMvc.perform(get("/twitter/api/users/" + user.getName() + "/timelines").contextPath("/twitter/api")
                .contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text", is("post one")))
                .andExpect(jsonPath("$[1].text", is("post two")))
                .andDo(document("timelines", preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("[].text").description("The text of the published post. "),
                                fieldWithPath("[].userName").description("The author of the post"),
                                fieldWithPath("[].dateTime").description("The date and time of publishing the post"))));
    }

    private static PostDTO createPost(String text, User author) {
        return new PostDTO(text, author.getName(), LocalDateTime.now());
    }

    private static byte[] convertObjectToJson(Object source) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(source);
    }

    private static <T> String getConstraints(Class<T> clazz, String property) {
        ConstraintDescriptions userConstraints = new ConstraintDescriptions(clazz);
        List<String> descriptions = userConstraints.descriptionsForProperty(property);

        StringJoiner stringJoiner = new StringJoiner(". ", "", ".");
        descriptions.forEach(stringJoiner::add);

        return stringJoiner.toString();
    }
}
