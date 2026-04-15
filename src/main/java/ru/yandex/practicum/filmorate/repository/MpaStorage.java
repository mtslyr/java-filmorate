package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaStorage {
    List<Mpa> findMpa();

    Mpa findMpaById(Long mpaId);

    void validateExist(Long... id);
}


