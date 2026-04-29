package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;


    public List<Mpa> getMpa() {
        return mpaStorage.findMpa();
    }

    public Mpa getMpaById(Long mpaId) {
        return mpaStorage.findMpaById(mpaId);
    }
}
