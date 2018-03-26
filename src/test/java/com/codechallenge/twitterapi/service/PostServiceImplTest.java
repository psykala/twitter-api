package com.codechallenge.twitterapi.service;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.codechallenge.twitterapi.dto.PostDTO;
import com.codechallenge.twitterapi.dto.UserDTO;
import com.codechallenge.twitterapi.model.Post;
import com.codechallenge.twitterapi.model.User;
import com.codechallenge.twitterapi.utils.UserPojoToDtoConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PostServiceImplTest {

    private PostService postService;

    private PostRepository postRepository;

    private UserRepository userRepository;

    @Before
    public void setUp() {
        postRepository = mock(PostRepository.class);
        userRepository = mock(UserRepository.class);
        postService = new PostServiceImpl(postRepository, userRepository);
    }

    @Test
    public void shouldCreatePostAndReturnNewlyCreatedPostWhenUserExists() {
        // given
        String userName = "userName";
        String postText = "postText";
        User user = new User(userName);
        Post post = mock(Post.class);

        when(post.getUser()).thenReturn(user);
        when(post.getText()).thenReturn(postText);
        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(postRepository.save(Mockito.any(Post.class))).thenReturn(post);

        // when
        PostDTO result = postService.addNewPost(postText, userName);

        // then
        verify(postRepository).save(Mockito.any(Post.class));
        assertNotNull(result);
    }

    @Test
    public void shouldCreatePostAndUserIfNotExistAndReturnNewlyCreatedPost() {
        // given
        String userName = "userName";
        String postText = "postText";
        User user = new User(userName);
        Post post = mock(Post.class);

        when(post.getUser()).thenReturn(user);
        when(userRepository.findByName(user.getName())).thenReturn(Optional.empty());
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        when(postRepository.save(Mockito.any(Post.class))).thenReturn(post);

        // when
        PostDTO result = postService.addNewPost(postText, userName);

        // then
        verify(postRepository).save(Mockito.any(Post.class));
        verify(userRepository).save(Mockito.any(User.class));
        assertNotNull(result);
    }

    @Test
    public void shouldReturnPostsForUserInReverseChronologicalOrder() {
        // given
        User user = new User("user1");
        List<Post> userPosts = createPostList(user);
        when(postRepository.findByUserName(user.getName())).thenReturn(userPosts);

        // when
        List<PostDTO> result = postService.findByUserName(user.getName());

        // then
        assertEquals(3, result.size());
        assertReverseChronologicalOrder(result);
    }

    @Test
    public void shouldReturnMultipleUserPostsInReverseChronologicalOrder() {
        // given
        User firstUser = new User("user1");
        User secondUser = new User("user2");
        UserDTO firstUserDto = UserPojoToDtoConverter.convert(firstUser);
        UserDTO secondUserDto = UserPojoToDtoConverter.convert(secondUser);
        List<Post> firstUserPosts = createPostList(firstUser);
        List<Post> secondUserPosts = createPostList(secondUser);
        when(postRepository.findByUserName(firstUser.getName())).thenReturn(firstUserPosts);
        when(postRepository.findByUserName(secondUser.getName())).thenReturn(secondUserPosts);

        // when
        List<PostDTO> result = postService.findPostsByUsers(Arrays.asList(firstUserDto, secondUserDto));

        // then
        assertEquals(6, result.size());
        assertReverseChronologicalOrder(result);
    }

    private void assertReverseChronologicalOrder(List<PostDTO> result) {
        for (int i = 0; i < result.size() - 1; i++) {
            PostDTO post1 = result.get(i);
            PostDTO post2 = result.get(i + 1);
            //@formatter:off
            assertFalse(post1.getDateTime().isBefore(post2.getDateTime()));
            //@formatter:on
        }
    }

    private List<Post> createPostList(User author) {
        Post post1 = new Post("Post no1", author, LocalDateTime.of(2018, Month.MARCH, 24, 0, 0));
        Post post2 = new Post("Post no2", author, LocalDateTime.of(2018, Month.MARCH, 8, 0, 0));
        Post post3 = new Post("Post no3", author, LocalDateTime.of(2018, Month.MARCH, 15, 0, 0));

        return Arrays.asList(post1, post2, post3);
    }
}
