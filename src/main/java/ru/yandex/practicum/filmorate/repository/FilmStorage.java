package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getAll();

    Film save(Film film);

    Film update(Film film) throws ApiException;


    Film getById(long id);
}
