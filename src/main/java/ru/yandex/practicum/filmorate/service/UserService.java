package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.exception.SameIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.request.UserRequest;
import ru.yandex.practicum.filmorate.model.response.Friend;
import ru.yandex.practicum.filmorate.model.response.UserResponse;
import ru.yandex.practicum.filmorate.repository.UserStorage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final UserMapper mapper;

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

    public UserResponse getUserById(Long userId) {
        User user = userStorage.getById(userId);
        return mapper.toResponse(user);
    }

    public UserResponse addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new SameIdException(userId, friendId);
        }

        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        user.getFriends().add(new Friend(friend));
        friend.getFriends().add(new Friend(user));
        return mapper.toResponse(user);
    }

    public UserResponse deleteFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new SameIdException(userId, friendId);
        }

        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        user.getFriends().removeIf(fr -> fr.getId().equals(friendId));
        friend.getFriends().removeIf(fr -> fr.getId().equals(userId));

        return mapper.toResponse(user);
    }

    public Set<Friend> getFriendsList(Long id) {
        User user = userStorage.getById(id);
        return user.getFriends();
    }

    public Set<Friend> getCommonFriends(Long userId, Long otherId) {
        if (userId.equals(otherId)) {
            throw new SameIdException(userId, otherId);
        }

        User user = userStorage.getById(userId);
        User other = userStorage.getById(otherId);

        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .collect(Collectors.toSet());
    }
}
