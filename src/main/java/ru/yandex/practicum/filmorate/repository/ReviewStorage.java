package ru.yandex.practicum.filmorate.repository;

import java.util.List;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    Review getById(Long id);

    List<Review> getAll(Long filmId, int count);

    void delete(Long id);

    void addLike(Long reviewId, Long userId);

    void removeLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void removeDislike(Long reviewId, Long userId);
}
