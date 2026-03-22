package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> getAllUsers() {
        return users.values();
    }

    public User createUser(User user) throws ApiException {
        validateEmailIsNotUsed(user);

        user.setId(getNextId());
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);

        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }

    public User updateUser(User user)
            throws ApiException {
        User oldUser = users.get(user.getId());

        if (oldUser == null) {
            throw new ApiException(
                    "Пользователь не найден",
                    "id",
                    user.getId().toString(),
                    HttpStatus.NOT_FOUND);
        }

        if (user.getName() != null) {
            oldUser.setName(user.getName());
        }

        if (user.getLogin() != null) {
            oldUser.setLogin(user.getLogin());
        }

        if (user.getEmail() != null) {
            validateEmailIsNotUsed(user);
            oldUser.setEmail(user.getEmail());
        }

        if (user.getBirthday() != null) {
            oldUser.setBirthday(user.getBirthday());
        }

        return oldUser;
    }

    private void validateEmailIsNotUsed(User user) throws ApiException {
        boolean isUsed = users.values()
                .stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()));

        if (isUsed) {
            throw new ApiException(
                    "Электронная почта уже используется",
                    "email",
                    user.getEmail(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
