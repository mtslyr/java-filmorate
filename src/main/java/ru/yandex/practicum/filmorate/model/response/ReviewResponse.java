package ru.yandex.practicum.filmorate.model.response;

public record ReviewResponse(
        Long reviewId,
        String content,
        Boolean isPositive,
        Long userId,
        Long filmId,
        Integer useful
) {
}