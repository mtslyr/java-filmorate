package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.exception.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getAll();

    Film save(Film film);

    Film update(Film film) throws ApiException;

    Film getById(long id);

    Collection<Film> getFavouriteFilms(long userId);

    /** Возвращает id пользователя, по которому делается рекомендация
     */
    Long getRecommenderId(long userId);

    void likeFilm(long userId, long filmId);

    void dislikeFilm(long userId, long filmId);

    Collection<Film> getFilmsByDirector(Long directorId, String sortBy);

    boolean delete(Long filmId) throws FilmNotFoundException;

    Collection<Film> search(String query, String by);
}
