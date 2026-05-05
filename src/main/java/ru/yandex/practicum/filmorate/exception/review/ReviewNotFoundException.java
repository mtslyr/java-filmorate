package ru.yandex.practicum.filmorate.exception.review;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.exception.ApiException;

public class ReviewNotFoundException extends ApiException {

    public ReviewNotFoundException(long id) {
        super(
                "Отзыв с таким ID не найден",
                "id",
                String.valueOf(id),
                HttpStatus.NOT_FOUND
        );
    }
}