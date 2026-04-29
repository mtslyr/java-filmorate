package ru.yandex.practicum.filmorate.exception.film;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.exception.ApiException;

public class InvalidMpaException extends ApiException {

    public InvalidMpaException(Long mpaId) {
        super("Неверный MPA", "id", mpaId.toString(), HttpStatus.NOT_FOUND);
    }
}
