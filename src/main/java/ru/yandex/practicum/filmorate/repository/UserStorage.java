package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> getAll();

    User save(User user);

    User update(User user) throws ApiException;

    User getById(long id);

    void validateExist(Long... id);

    boolean delete(Long userId) throws UserNotFoundException;
}
