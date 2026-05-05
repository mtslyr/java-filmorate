package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorStorage {
    Collection<Director> getAll();

    Director save(Director director);

    Director update(Director director) throws ApiException;

    Director getById(long id);

    void deleteDirector(long directorId);

    void validateExist(Long... id);

}
