package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.request.FilmRequest;
import ru.yandex.practicum.filmorate.model.response.FilmResponse;
import ru.yandex.practicum.filmorate.repository.FilmStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmMapper mapper;

    public FilmService(@Qualifier("H2FilmStorage")FilmStorage filmStorage,
                       FilmMapper mapper) {
        this.filmStorage = filmStorage;
        this.mapper = mapper;
    }

    public Collection<FilmResponse> getAllFilms() {
        return filmStorage.getAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public FilmResponse createFilm(FilmRequest request) throws ApiException {
        Film film = mapper.toFilm(request);
        validateReleaseDate(film);
        film = filmStorage.save(film);
        return mapper.toResponse(film);
    }

    public FilmResponse updateFilm(FilmRequest request) throws ApiException {
        Film film = mapper.toFilm(request);
        if (film.getReleaseDate() != null) {
            validateReleaseDate(film);
        }

        film = filmStorage.update(film);
        return mapper.toResponse(film);
    }

    public FilmResponse getById(Long id) {
        Film film = filmStorage.getById(id);
        return mapper.toResponse(film);
    }

    public FilmResponse likeFilm(Long filmId, Long userId) {
        filmStorage.likeFilm(userId, filmId);
        return mapper.toResponse(filmStorage.getById(filmId));
    }

    public FilmResponse dislikeFilm(Long filmId, Long userId) {
        filmStorage.dislikeFilm(userId, filmId);
        return mapper.toResponse(filmStorage.getById(filmId));
    }

    public Collection<FilmResponse> getPopularFilms(Integer count) {
        return filmStorage.getAll()
                .stream()
                .sorted(Comparator.comparingInt((Film f)  -> f.getLikes().size()).reversed())
                .limit(count)
                .map(mapper::toResponse)
                .toList();
    }

    private void validateReleaseDate(Film film) {
        final String fieldName = "releaseDate";
        final Predicate<Film> validateFilmReleaseDate =
                f -> f.getReleaseDate()
                        .isAfter(LocalDate.of(1895, 12, 28));

        if (!validateFilmReleaseDate.test(film)) {
            throw new ApiException(
                    "Дата релиза должна быть не ранее 28-12-1895",
                    fieldName,
                    film.getReleaseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    HttpStatus.BAD_REQUEST);
        }
    }

    public List<Genre> getGenres() {
        return filmStorage.findGenres();
    }

    public Genre getGenreById(Long genreId) {
        return filmStorage.findGenreById(genreId);
    }

    public List<Mpa> getMpa() {
        return filmStorage.findMpa();
    }

    public Mpa getMpaById(Long mpaId) {
        return filmStorage.findMpaById(mpaId);
    }
}
