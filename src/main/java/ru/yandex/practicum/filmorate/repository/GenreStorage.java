package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {
    List<Genre> findGenres();

    Genre findGenreById(Long genreId);

    List<Genre> findGenres(List<Long> genresIds);

    void validateExist(List<Long> id);
}
