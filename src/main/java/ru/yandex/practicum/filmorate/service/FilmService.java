package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.film.InvalidReleaseDateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.request.FilmRequest;
import ru.yandex.practicum.filmorate.model.response.FilmResponse;
import ru.yandex.practicum.filmorate.repository.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmMapper mapper;


    public FilmService(
            @Qualifier("H2FilmStorage") FilmStorage filmStorage,
            FilmMapper mapper) {
        this.filmStorage = filmStorage;
        this.mapper = mapper;
    }

    public Collection<FilmResponse> getAllFilms() {
        return filmStorage.getAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public Collection<FilmResponse> getPopularFilms(Integer count) {
        return filmStorage.getAll()
                .stream()
                .sorted(Comparator.comparingInt((Film f)  -> f.getLikes().size()).reversed())
                .limit(count)
                .map(mapper::toResponse)
                .toList();
    }

    public FilmResponse getById(Long id) {
        return mapper.toResponse(filmStorage.getById(id));
    }

    public FilmResponse createFilm(FilmRequest request) {
        validateFilmReleaseDate(request);
        Film created = filmStorage.save(mapper.toFilm(request));
        return mapper.toResponse(created);
    }

    public FilmResponse updateFilm(FilmRequest request) {
        if (request.getReleaseDate() != null) {
            validateFilmReleaseDate(request);
        }


        Film updated = filmStorage.update(mapper.toFilm(request));

        return mapper.toResponse(updated);
    }

    public FilmResponse likeFilm(Long filmId, Long userId) {
        filmStorage.likeFilm(userId, filmId);
        return mapper.toResponse(filmStorage.getById(filmId));
    }

    public FilmResponse dislikeFilm(Long filmId, Long userId) {
        filmStorage.dislikeFilm(userId, filmId);
        return mapper.toResponse(filmStorage.getById(filmId));
    }

    public void validateFilmReleaseDate(FilmRequest request) {
        if (request.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new InvalidReleaseDateException(request.getReleaseDate());
        }
    }
}
