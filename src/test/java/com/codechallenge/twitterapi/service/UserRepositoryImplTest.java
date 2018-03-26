package com.codechallenge.twitterapi.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.codechallenge.twitterapi.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserRepositoryImplTest {

    @InjectMocks
    private UserRepository userRepository = new UserRepositoryImpl();

    @Mock
    private Map<String, User> allUsers;

    @Test
    public void shouldReturnNewlyAddedUser() {
        // given
        User user = new User("newUser");

        // when
        User result = userRepository.save(user);

        // then
        assertNotNull(result);
        verify(allUsers).put(user.getName()
                .toLowerCase(), user);
    }

    @Test
    public void shouldReturnEmptyResultWhenNoUserWithGivenNameFound() {
        // given
        String userName = "nonExistingUserName";
        when(allUsers.get(Mockito.any(String.class))).thenReturn(null);

        // when
        Optional<User> result = userRepository.findByName(userName);

        // then
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnNonEmptyResultWhenUserFound() {
        // given
        String userName = "existingUserName";
        User expectedUser = new User(userName);
        when(allUsers.get(Mockito.any(String.class))).thenReturn(expectedUser);

        // when
        Optional<User> result = userRepository.findByName(userName);

        // then
        assertTrue(result.isPresent());
        assertEquals(userName, result.get()
                .getName());
    }

    @Test
    public void shouldReturnAllUsersIfAnyExist() {
        // given
        User user1 = new User("user1");
        User user2 = new User("user2");
        List<User> expectedUserList = Arrays.asList(user1, user2);
        when(allUsers.values()).thenReturn(expectedUserList);

        // when
        List<User> result = userRepository.findAll();

        // then
        assertEquals(expectedUserList.size(), result.size());
    }

    @Test
    public void shouldReturnEmptyListWhenNoUsers() {
        // given
        when(allUsers.values()).thenReturn(Collections.emptyList());

        // when
        List<User> result = userRepository.findAll();

        // then
        verify(allUsers).values();
        assertTrue(result.isEmpty());

    }

    @Test
    public void shouldReturnEmptyListWhenUserDontFollowOthers() {
        // given
        User user = new User("user");
        when(allUsers.get(user.getName())).thenReturn(user);

        // when
        List<User> result = userRepository.findAllFollowedUsers(user.getName());

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnNonEmptyListWhenUserFollowsOthers() {
        // given
        String userName = "user2";
        User user1 = new User("user1");
        User user2 = new User(userName);
        User user3 = new User("user3");

        user2.follow(user1);
        user2.follow(user3);
        when(allUsers.get(userName)).thenReturn(user2);

        // when
        List<User> result = userRepository.findAllFollowedUsers(user2.getName());

        // then
        verify(allUsers).get(userName);
        assertEquals(2, result.size());
    }
}
