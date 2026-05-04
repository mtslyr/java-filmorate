package ru.yandex.practicum.filmorate.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.film.InvalidReleaseDateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.request.FilmRequest;
import ru.yandex.practicum.filmorate.model.response.FilmResponse;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.service.FeedService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmMapper mapper;
    private final FeedService feedService;

    public FilmService(
            @Qualifier("H2FilmStorage") FilmStorage filmStorage,
            FilmMapper mapper, FeedService feedService) {
        this.filmStorage = filmStorage;
        this.mapper = mapper;
        this.feedService = feedService;
    }

    public Collection<FilmResponse> getAllFilms() {
        return filmStorage.getAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public Collection<FilmResponse> getPopularFilms(Integer count) {
        return sortByPopular(filmStorage.getAll())
                .stream()
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

        feedService.addEvent(new FeedEvent(
                null,
                System.currentTimeMillis(),
                userId,
                EventType.LIKE,
                OperationType.ADD,
                filmId
        ));

        return mapper.toResponse(filmStorage.getById(filmId));
    }

    public FilmResponse dislikeFilm(Long filmId, Long userId) {
        filmStorage.dislikeFilm(userId, filmId);

        feedService.addEvent(new FeedEvent(
                null,
                System.currentTimeMillis(),
                userId,
                EventType.LIKE,
                OperationType.REMOVE,
                filmId
        ));

        return mapper.toResponse(filmStorage.getById(filmId));
    }

    public void validateFilmReleaseDate(FilmRequest request) {
        if (request.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new InvalidReleaseDateException(request.getReleaseDate());
        }
    }

    public Collection<FilmResponse> getCommonFilms(Long userId, Long friendId) {
        Collection<Film> userFilms = filmStorage.getFavouriteFilms(userId);
        Collection<Film> friendFilms = filmStorage.getFavouriteFilms(friendId);
        Collection<Film> commonFilms = CollectionUtils.intersection(userFilms, friendFilms);

        return sortByPopular(commonFilms)
                .stream().map(mapper::toResponse)
                .toList();
    }

    private Collection<Film> sortByPopular(Iterable<Film> films) {
        Spliterator<Film> spliterator = films.spliterator();
        return StreamSupport.stream(spliterator, false)
                .sorted(Comparator.comparingInt((Film f)  -> f.getLikes().size()).reversed())
                .toList();
    }
}
