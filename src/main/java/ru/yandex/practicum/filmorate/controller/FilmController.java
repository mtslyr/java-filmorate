package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.request.FilmRequest;
import ru.yandex.practicum.filmorate.model.response.FilmResponse;
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
    private final FilmMapper mapper;

    @GetMapping
    public Collection<FilmResponse> getFilms() {
        log.info("Получить список фильмов");
        return filmService.getAllFilms()
                .stream()
                .map(f -> mapper.toResponse(f))
                .toList();
    }

    @GetMapping("/popular")
    public Collection<FilmResponse> getPopularFilms(@RequestParam(name = "count", required = false, defaultValue = "10") int count) {
        log.info("Получить {} популярных фильмов", count);
        return filmService.getPopularFilms(count)
                .stream()
                .map(f -> mapper.toResponse(f))
                .toList();
    }

    @GetMapping("/{id}")
    public FilmResponse getFilmById(@PathVariable("id") Long id) {
        log.info("Получить фильм по ID: {}", id);
        return mapper.toResponse(filmService.getById(id));
    }

    @PostMapping
    public FilmResponse createFilm(@RequestBody @Validated(OnCreate.class) FilmRequest request) {
        log.info("Создание фильма: {}", request);
        Film result = filmService.createFilm(mapper.toFilm(request));
        return mapper.toResponse(result);
    }

    @PutMapping
    public FilmResponse updateFilm(@RequestBody @Validated(OnUpdate.class) FilmRequest request) {
        log.info("Обновление фильма: {}", request);
        Film result = filmService.updateFilm(mapper.toFilm(request));
        return mapper.toResponse(result);
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmResponse likeFilm(
            @PathVariable("id") long filmId,
            @PathVariable("userId") long userId) {
        log.info("Пользователь {} лайкнул фильм {}", userId, filmId);
        Film result = filmService.likeFilm(filmId, userId);
        return mapper.toResponse(result);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmResponse dislikeFilm(
            @PathVariable("id") long filmId,
            @PathVariable("userId") long userId) {
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
        Film result = filmService.dislikeFilm(filmId, userId);
        return mapper.toResponse(result);
    }
}
