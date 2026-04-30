package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface Recommendation {
    Collection<Film> getRecommendations(Long userId);
}
