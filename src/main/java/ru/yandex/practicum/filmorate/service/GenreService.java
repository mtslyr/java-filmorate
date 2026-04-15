package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;


    public List<Genre> getGenres() {
        return genreStorage.findGenres();
    }

    public Genre getGenreById(Long genreId) {
        return genreStorage.findGenreById(genreId);
    }
}
