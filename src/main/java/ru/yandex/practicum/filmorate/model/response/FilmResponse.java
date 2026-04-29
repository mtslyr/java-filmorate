package ru.yandex.practicum.filmorate.model.response;


import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record FilmResponse(
        Long id,
        String name,
        String description,
        LocalDate releaseDate,
        Integer duration,
        Set<Long> likes,
        List<Genre> genres,
        Mpa mpa
) {
}
