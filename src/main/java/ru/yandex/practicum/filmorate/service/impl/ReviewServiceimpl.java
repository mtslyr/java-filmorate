package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceimpl implements ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Override
    public Review create(Review review) {
        userStorage.getById(review.getUserId());
        filmStorage.getById(review.getFilmId());
        return reviewStorage.create(review);
    }

    @Override
    public Review update(Review review) {
        reviewStorage.getById(review.getReviewId());
        return reviewStorage.update(review);
    }

    @Override
    public Review getById(Long id) {
        return reviewStorage.getById(id);
    }

    @Override
    public List<Review> getAll(Long filmId, int count) {
        return reviewStorage.getAll(filmId, count);
    }

    @Override
    public void delete(Long id) {
        reviewStorage.delete(id);
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        reviewStorage.addLike(reviewId, userId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        reviewStorage.addDislike(reviewId, userId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        reviewStorage.removeLike(reviewId, userId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        reviewStorage.removeDislike(reviewId, userId);
    }
}