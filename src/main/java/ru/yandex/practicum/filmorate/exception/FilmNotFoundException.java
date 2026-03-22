package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;

public class FilmNotFoundException extends ApiException {
    public FilmNotFoundException(long id) {
        super("Фильм не найден", "id", String.valueOf(id), HttpStatus.NOT_FOUND);
    }
}
