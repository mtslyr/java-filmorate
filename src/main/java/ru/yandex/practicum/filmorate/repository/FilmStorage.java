package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Collection<Film> getAll();

    Film save(Film film);

    Film update(Film film) throws ApiException;


    Film getById(long id);

    void likeFilm(long userId, long filmId);

    void dislikeFilm(long userId, long filmId);

    List<Genre> findGenres();

    Genre findGenreById(Long genreId);

    List<Mpa> findMpa();

    Mpa findMpaById(Long mpaId);
}
