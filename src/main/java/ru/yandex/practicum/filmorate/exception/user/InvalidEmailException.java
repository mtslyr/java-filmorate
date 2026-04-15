package ru.yandex.practicum.filmorate.exception.user;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.exception.ApiException;

public class InvalidEmailException extends ApiException {

    public InvalidEmailException(String email) {
        super("Электронная почта '%s' уже используется".formatted(email), "email", email, HttpStatus.BAD_REQUEST);
    }
}
