package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.exception.user.SameIdException;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.request.UserRequest;
import ru.yandex.practicum.filmorate.model.response.Friend;
import ru.yandex.practicum.filmorate.model.response.UserResponse;
import ru.yandex.practicum.filmorate.repository.UserStorage;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    @Qualifier("H2UserStorage")
    private final UserStorage userStorage;
    private final UserMapper mapper;

    public UserService(@Qualifier("H2UserStorage") UserStorage userStorage, UserMapper mapper) {
        this.userStorage = userStorage;
        this.mapper = mapper;
    }

    public Collection<UserResponse> getAllUsers() {
        return userStorage.getAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public UserResponse createUser(UserRequest request)  {
        User user = mapper.toUser(request);
        validate(user);
        User saved = userStorage.save(user);
        return mapper.toResponse(saved);
    }

    public UserResponse updateUser(UserRequest request) {
        userStorage.getById(request.getId());
        User user = mapper.toUser(request);

        if (user.getEmail() != null) {
            validateEmailIsNotUsed(user);
        }

        User updated = userStorage.update(user);
        return mapper.toResponse(updated);
    }

    public UserResponse getUserById(Long userId) {
        User user = userStorage.getById(userId);
        return mapper.toResponse(user);
    }

    public UserResponse addFriend(Long userId, Long friendId) {
        validateUserExist(userId, friendId);

        if (userId.equals(friendId)) {
            throw new SameIdException(userId, friendId);
        }

        userStorage.addFriend(userId, friendId);
        return mapper.toResponse(userStorage.getById(userId));
    }

    public UserResponse deleteFriend(Long userId, Long friendId) {
        validateUserExist(userId, friendId);

        if (userStorage.usersAreFriends(userId, friendId)) {
            if (userId.equals(friendId)) {
                throw new SameIdException(userId, friendId);
            }

            userStorage.deleteFriend(userId, friendId);
        }

        User user = userStorage.getById(userId);
        return mapper.toResponse(user);
    }

    public Set<Friend> getFriendsList(Long id) {
        validateUserExist(id);
        return userStorage.getUserFriends(id);
    }

    public Set<Friend> getCommonFriends(Long userId, Long otherId) {
        validateUserExist(userId, otherId);

        if (userId.equals(otherId)) {
            throw new SameIdException(userId, otherId);
        }

        Set<Friend> friends = userStorage.getUserFriends(userId);
        Set<Friend> otherFriends = userStorage.getUserFriends(otherId);

        return friends.stream()
                .filter(otherFriends::contains)
                .collect(Collectors.toSet());
    }

    private void validateEmailIsNotUsed(User user) {
        boolean isUsed = userStorage.getAll()
                .stream()
                .anyMatch(u -> !u.getId().equals(user.getId()) && u.getEmail().equals(user.getEmail()));

        if (isUsed) {
            throw new ApiException(
                    "Электронная почта уже используется",
                    "email",
                    user.getEmail(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validate(User user) {
        validateEmailIsNotUsed(user);
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
    }

    private void validateUserExist(Long... id) {
        for (Long l : id) {
            try {
                userStorage.getById(l);
            } catch (NoSuchElementException e) {
                throw new UserNotFoundException(l);
            }
        }
    }
}
