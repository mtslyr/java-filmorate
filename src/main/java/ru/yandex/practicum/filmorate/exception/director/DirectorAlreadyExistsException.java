package ru.yandex.practicum.filmorate.exception.director;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.exception.ApiException;

public class DirectorAlreadyExistsException extends ApiException {
    public DirectorAlreadyExistsException(String name) {
        super("Режиссёр уже существует", "name", name, HttpStatus.CONFLICT);
    }
}