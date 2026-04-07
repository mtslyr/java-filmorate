package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.request.FilmRequest;
import ru.yandex.practicum.filmorate.model.response.FilmResponse;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FilmMapper mapper;

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

    public FilmResponse getById(Long id) {
        Film film = filmStorage.getById(id);
        return mapper.toResponse(film);
    }

    public FilmResponse likeFilm(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId);
        film.getLikes().add(userId);
        User user = userStorage.getById(userId);
        user.getFavouriteFilms().add(film);
        return mapper.toResponse(film);
    }

    public FilmResponse dislikeFilm(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId);
        film.getLikes().remove(userId);
        User user = userStorage.getById(userId);
        user.getFavouriteFilms().removeIf(f -> f.getId().equals(filmId));
        return mapper.toResponse(film);
    }

    public Collection<FilmResponse> getPopularFilms(Integer count) {
        return filmStorage.getAll()
                .stream()
                .sorted(Comparator.comparingInt((Film f)  -> f.getLikes().size()).reversed())
                .limit(count)
                .map(mapper::toResponse)
                .toList();
    }
}
