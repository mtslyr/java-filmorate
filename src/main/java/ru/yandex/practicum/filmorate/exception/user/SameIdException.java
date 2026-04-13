package ru.yandex.practicum.filmorate.exception.user;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.exception.ApiException;

public class SameIdException extends ApiException {

    public SameIdException(Long id, Long otherId) {
        super("Получены одинаковые ID %d : %d".formatted(id, otherId), "id : otherId", otherId.toString(), HttpStatus.BAD_REQUEST);
    }
}
