package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmNotFountException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Predicate;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Получить список фильмов");
        return filmService.getAllFilms();
    }

    @PostMapping
    public Film createFilm(@RequestBody @Validated(OnCreate.class) Film film) {
        log.info("Создание фильма: {}", film);
        try {
            validateReleaseDate(film);
            return filmService.createFilm(film);
        } catch (ValidationException e) {
            log.info("Ошибка во время создания фильма:\n{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Validated(OnUpdate.class) Film film) {
        log.info("Обновление фильма: {}", film);
        validateReleaseDate(film);
        try {
            return filmService.updateFilm(film);
        } catch (FilmNotFountException e) {
            log.info("Ошибка во время обновления фильма:\n{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void validateReleaseDate(Film film) {
        final Predicate<Film> validateFilmReleaseDate =
                f -> f.getReleaseDate()
                        .isAfter(LocalDate.of(1895, 12, 28));

        if (film.getReleaseDate() != null) {
            if (!validateFilmReleaseDate.test(film)) {
                log.info("Release date validation failed: {}", film.getReleaseDate());
                throw new ValidationException("Дата релиза должна быть не ранее 28-12-1895");
            }
        }
    }
}
