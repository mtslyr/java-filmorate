package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException(long id) {
        super("Пользователь не найден", "id", String.valueOf(id), HttpStatus.NOT_FOUND);
    }
}
