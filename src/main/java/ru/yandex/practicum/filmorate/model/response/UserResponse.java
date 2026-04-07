package ru.yandex.practicum.filmorate.model.response;

import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        String login,
        String name,
        LocalDate birthday,
        Set<Friend> friends,
        Set<Film> favouriteFilms
) {
}
