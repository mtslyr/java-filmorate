package ru.yandex.practicum.filmorate.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmStorage;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationImpl implements Recommendation {

    private final FilmStorage filmStorage;

    @Override
    public Collection<Film> getRecommendations(Long userId) {
        Long recommenderId = filmStorage.getRecommenderId(userId);
        Collection<Film> userFavouriteFilms = filmStorage.getFavouriteFilms(userId);
        Collection<Film> recommenderFavouriteFilms = filmStorage.getFavouriteFilms(recommenderId);

        return CollectionUtils.removeAll(recommenderFavouriteFilms, userFavouriteFilms);
    }
}
