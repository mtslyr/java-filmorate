package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.request.FilmRequest;
import ru.yandex.practicum.filmorate.model.response.FilmResponse;
import ru.yandex.practicum.filmorate.service.impl.FilmService;
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
    public Collection<FilmResponse> getFilms() {
        log.info("Получить список фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/popular")
    public Collection<FilmResponse> getPopularFilms(
            @RequestParam(name = "count", required = false, defaultValue = "10") Integer count) {
        log.info("Получить {} популярных фильмов", count);
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/director/{directorId}")
    public Collection<FilmResponse> getFilmsByDirector(
            @PathVariable Long directorId,
            @RequestParam String sortBy) {
        log.info("Получить фильмы режиссера {} с сортировкой {}", directorId, sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public Collection<FilmResponse> search(
            @RequestParam String query,
            @RequestParam String by
    ) {
        log.info("Поиск фильмов по query='{}', by='{}'", query, by);
        return filmService.search(query, by);
    }

    @GetMapping("/{id}")
    public FilmResponse getFilmById(@PathVariable("id") Long id) {
        log.info("Получить фильм по ID: {}", id);
        return filmService.getById(id);
    }

    @PostMapping
    public FilmResponse createFilm(@RequestBody @Validated(OnCreate.class) FilmRequest request) {
        log.info("Создание фильма: {}", request);
        return filmService.createFilm(request);
    }

    @PutMapping
    public FilmResponse updateFilm(@RequestBody @Validated(OnUpdate.class) FilmRequest request) {
        log.info("Обновление фильма: {}", request);
        return filmService.updateFilm(request);
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmResponse likeFilm(
            @PathVariable("id") Long filmId,
            @PathVariable("userId") Long userId) {
        log.info("Пользователь {} лайкнул фильм {}", userId, filmId);
        return filmService.likeFilm(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmResponse dislikeFilm(
            @PathVariable("id") Long filmId,
            @PathVariable("userId") Long userId) {
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
        return filmService.dislikeFilm(filmId, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable("id") Long filmId) {
        log.info("Удалить фильм по ID: {}", filmId);
        filmService.deleteFilm(filmId);
    }

    @GetMapping("/common")
    public Collection<FilmResponse> getCommonFilms(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "friendId") Long friendId) {
        log.info("Поиск общих фильмов для пользователей {} и {}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }
}
