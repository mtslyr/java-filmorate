package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class H2ReviewStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        return review;
    }

    @Override
    public Review update(Review review) {
        return review;
    }

    @Override
    public Review getById(Long id) {
        return null;
    }

    @Override
    public List<Review> getAll(Long filmId, int count) {
        return List.of();
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public void addLike(Long reviewId, Long userId) {

    }

    @Override
    public void removeLike(Long reviewId, Long userId) {

    }

    @Override
    public void addDislike(Long reviewId, Long userId) {

    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {

    }
}
