package ru.yandex.practicum.filmorate.exception.film;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.exception.ApiException;

public class InvalidGenreException extends ApiException {
    public InvalidGenreException(Long genreId) {
        super("Неверный жанр", "id", genreId.toString(), HttpStatus.NOT_FOUND);
    }
}
