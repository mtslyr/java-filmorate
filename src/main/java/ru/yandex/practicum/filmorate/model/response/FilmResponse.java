package ru.yandex.practicum.filmorate.model.response;


import java.time.LocalDate;
import java.util.Set;

public record FilmResponse(
        Long id,
        String name,
        String description,
        LocalDate releaseDate,
        Integer duration,
        Set<Long> likes
) {
}
