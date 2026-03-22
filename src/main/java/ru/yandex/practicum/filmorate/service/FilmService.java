package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
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

    public Collection<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public Film createFilm(Film film) throws ApiException {
        validateReleaseDate(film);
        return filmStorage.save(film);
    }

    public Film updateFilm(Film film) throws ApiException {
        if (film.getReleaseDate() != null) {
            validateReleaseDate(film);
        }

        return filmStorage.update(film);
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

    public Film getById(Long id) {
        Film film = filmStorage.getById(id);
        return film;
    }

    public Film likeFilm(long filmId, long userId) {
        Film film = filmStorage.getById(filmId);
        film.getLikes().add(userId);
        User user = userStorage.getById(userId);
        user.getFavouriteFilms().add(film);
        return film;
    }

    public Film dislikeFilm(long filmId, long userId) {
        Film film = filmStorage.getById(filmId);
        film.getLikes().remove(userId);
        User user = userStorage.getById(userId);
        user.getFavouriteFilms().remove(filmId);
        return film;
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.getAll()
                .stream()
                .sorted(Comparator.comparingInt((Film f)  -> f.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }
}
