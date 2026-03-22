package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final Map<Long, Film> films = new HashMap<>();

    public Collection<Film> getAllFilms() {
        return films.values();
    }

    public Film createFilm(Film film) throws ApiException {
        validateReleaseDate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    public Film updateFilm(Film film)
            throws ApiException {
        Film oldFilm = films.get(film.getId());

        if (oldFilm == null) {
            throw new ApiException(
                    "Фильм не найден",
                    "id",
                    film.getId().toString(),
                    HttpStatus.NOT_FOUND
            );
        }

        if (film.getDescription() != null) {
            oldFilm.setDescription(film.getDescription());
        }

        if (film.getName() != null) {
            oldFilm.setName(film.getName());
        }

        if (film.getReleaseDate() != null) {
            validateReleaseDate(film);
            oldFilm.setReleaseDate(film.getReleaseDate());
        }

        if (film.getDuration() != null) {
            oldFilm.setDuration(film.getDuration());
        }

        return oldFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
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

    public Map<Long, Film> getStorage() {
        return Map.copyOf(films);
    }
}
