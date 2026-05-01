package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.service.RecommendationService;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final FilmStorage filmStorage;

    @Override
    public Collection<Film> getRecommendations(Long userId) {
        try {
            Long recommenderId = filmStorage.getRecommenderId(userId);
            Collection<Film> userFavouriteFilms = filmStorage.getFavouriteFilms(userId);
            Collection<Film> recommenderFavouriteFilms = filmStorage.getFavouriteFilms(recommenderId);

            return CollectionUtils.removeAll(recommenderFavouriteFilms, userFavouriteFilms);
        } catch (EmptyResultDataAccessException e) {
            log.info("Не найдены рекомендатели для пользователя {}", userId);
            return Collections.emptySet();
        }
    }
}
