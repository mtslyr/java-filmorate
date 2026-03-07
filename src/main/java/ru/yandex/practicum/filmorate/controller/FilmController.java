package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.util.Collection;

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
            return filmService.createFilm(film);
        } catch (Exception e) {
            log.info("Ошибка во время создания фильма:\n{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Validated(OnUpdate.class) Film film) {
        log.info("Обновление фильма: {}", film);
        try {
            return filmService.updateFilm(film);
        } catch (Exception e) {
            log.info("Ошибка во время обновления фильма:\n{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
