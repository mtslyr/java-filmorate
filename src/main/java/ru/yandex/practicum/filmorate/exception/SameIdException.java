package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;

public class SameIdException extends ApiException {

    public SameIdException(Long id, Long otherId) {
        super("Получены одинаковые ID %d : %d".formatted(id, otherId), "id : otherId", otherId.toString(), HttpStatus.BAD_REQUEST);
    }
}
