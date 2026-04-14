package ru.yandex.practicum.filmorate.exception.user;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.exception.ApiException;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException(long id) {
        super("Пользователь не найден", "id", String.valueOf(id), HttpStatus.NOT_FOUND);
    }
}
