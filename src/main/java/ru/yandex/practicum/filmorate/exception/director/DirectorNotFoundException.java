package ru.yandex.practicum.filmorate.exception.director;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.exception.ApiException;

public class DirectorNotFoundException extends ApiException {
    public DirectorNotFoundException(long id) {
        super("Режиссер не найден", "id", String.valueOf(id), HttpStatus.NOT_FOUND);
    }
}
