package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EmailAlreadyUsedException;
import ru.yandex.practicum.filmorate.exception.InvalidBirthDateException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
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

    public User createUser(User user) throws InvalidBirthDateException, EmailAlreadyUsedException {
        validateBirthDay(user);
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
            throws UserNotFoundException, EmailAlreadyUsedException, InvalidBirthDateException {
        User oldUser = users.get(user.getId());

        if (oldUser == null) {
            throw new UserNotFoundException("Пользователь с id = %d не найден".formatted(user.getId()));
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
            validateBirthDay(user);
            oldUser.setBirthday(user.getBirthday());
        }

        return oldUser;
    }

    private void validateEmailIsNotUsed(User user) throws EmailAlreadyUsedException {
        boolean isUsed = users.values()
                .stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()));

        if (isUsed) {
            throw new EmailAlreadyUsedException("Электронная почта %s уже используется".formatted(user.getEmail()));
        }
    }

    private void validateBirthDay(User user) throws InvalidBirthDateException {
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new InvalidBirthDateException("Дата рождения не может быть в будущем");
        }
    }
}
