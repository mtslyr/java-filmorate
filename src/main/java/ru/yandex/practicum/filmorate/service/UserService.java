package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.response.Friend;
import ru.yandex.practicum.filmorate.repository.UserStorage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public Collection<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User createUser(User user)  {
        validate(user);
        return userStorage.save(user);
    }

    public User updateUser(User user) {
        userStorage.getById(user.getId());

        if (user.getEmail() != null) {
            validateEmailIsNotUsed(user);
        }

        return userStorage.update(user);
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

    public User getUserById(Long userId) {
        return userStorage.getById(userId);
    }

    public User addFriend(long userId, long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        user.getFriends().add(new Friend(friend));
        friend.getFriends().add(new Friend(user));
        return user;
    }

    public User deleteFriend(long userId, long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        user.getFriends().removeIf(fr -> fr.getId().equals(friendId));
        friend.getFriends().removeIf(fr -> fr.getId().equals(userId));

        return user;
    }

    public Set<Friend> getFriendsList(long id) {
        User user = userStorage.getById(id);
        return user.getFriends();
    }

    public Set<Friend> getCommonFriends(long userId, long otherId) {
        User user = userStorage.getById(userId);
        User other = userStorage.getById(otherId);

        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .collect(Collectors.toSet());
    }
}
