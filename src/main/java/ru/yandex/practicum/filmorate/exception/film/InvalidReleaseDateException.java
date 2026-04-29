package ru.yandex.practicum.filmorate.exception.film;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.exception.ApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InvalidReleaseDateException extends ApiException {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public InvalidReleaseDateException(LocalDate receivedValue) {
        super("Дата выхода фильма должна быть не раньше 1895-12-28", "releaseDate", dateOfPattern(receivedValue), HttpStatus.BAD_REQUEST);
    }

    private static String dateOfPattern(LocalDate date) {
        return date.format(FORMATTER);
    }
}
