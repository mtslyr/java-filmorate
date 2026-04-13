package ru.yandex.practicum.filmorate.exception.film;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.exception.ApiException;

public class FilmNotFoundException extends ApiException {
    public FilmNotFoundException(long id) {
        super("Фильм не найден", "id", String.valueOf(id), HttpStatus.NOT_FOUND);
    }
}
