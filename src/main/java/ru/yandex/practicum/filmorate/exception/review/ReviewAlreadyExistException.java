package ru.yandex.practicum.filmorate.exception.review;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.exception.ApiException;

public class ReviewAlreadyExistException extends ApiException {
    public ReviewAlreadyExistException(Long filmId) {
        super("Пользователь уже оставил отзыв на этот фильм",
                "filmId",
                String.valueOf(filmId),
                HttpStatus.CONFLICT);
    }
}
