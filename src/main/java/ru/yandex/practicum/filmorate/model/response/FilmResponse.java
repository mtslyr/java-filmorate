package ru.yandex.practicum.filmorate.model.response;


import ru.yandex.practicum.filmorate.model.enums.FilmGenre;
import ru.yandex.practicum.filmorate.model.enums.FilmRating;

import java.time.LocalDate;
import java.util.Set;

public record FilmResponse(
        Long id,
        String name,
        String description,
        LocalDate releaseDate,
        Integer duration,
        Set<Long> likes,
        FilmGenre genre,
        FilmRating rating
) {
}
