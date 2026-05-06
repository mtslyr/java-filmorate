package ru.yandex.practicum.filmorate.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.film.InvalidReleaseDateException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.request.FeedRequest;
import ru.yandex.practicum.filmorate.model.request.FilmRequest;
import ru.yandex.practicum.filmorate.model.response.FilmResponse;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.service.FeedService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final FeedService feedService;
    private final FilmMapper mapper;

    public FilmService(
            @Qualifier("H2FilmStorage") FilmStorage filmStorage,
            FeedService feedService,
            FilmMapper mapper) {
        this.filmStorage = filmStorage;
        this.feedService = feedService;
        this.mapper = mapper;
    }

    public List<FilmResponse> getAllFilms() {
        return filmStorage.getAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public Collection<FilmResponse> getPopularFilms(Integer count, Long genreId, Integer year) {
        return sortByPopular(filmStorage.getAll(genreId, year))
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

    public Collection<FilmResponse> getFilmsByDirector(Long directorId, String sortBy) {
        return filmStorage.getFilmsByDirector(directorId, sortBy)
                .stream()
                .map(mapper::toResponse)
                .toList();
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
        FeedRequest event = new FeedRequest(
                System.currentTimeMillis(),
                userId,
                EventType.LIKE,
                OperationType.ADD,
                filmId
        );
        feedService.addEvent(event);
        return mapper.toResponse(filmStorage.getById(filmId));
    }

    public FilmResponse dislikeFilm(Long filmId, Long userId) {
        filmStorage.dislikeFilm(userId, filmId);
        FeedRequest event = new FeedRequest(
                System.currentTimeMillis(),
                userId,
                EventType.LIKE,
                OperationType.REMOVE,
                filmId
        );
        feedService.addEvent(event);
        return mapper.toResponse(filmStorage.getById(filmId));
    }

    public void validateFilmReleaseDate(FilmRequest request) {
        if (request.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new InvalidReleaseDateException(request.getReleaseDate());
        }
    }

    public boolean deleteFilm(Long filmId) {
        return filmStorage.delete(filmId);
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

     public Collection<FilmResponse> search(String query, String by) {
        return filmStorage.search(query, by)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
