package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.FeedStorage;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedStorage feedStorage;

    @Override
    public Review create(Review review) {
        userStorage.getById(review.getUserId());
        filmStorage.getById(review.getFilmId());
        Review created = reviewStorage.create(review);
        feedStorage.addEvent(created.getUserId(), created.getReviewId(), "REVIEW", "ADD");
        return created;
    }

    @Override
    public Review update(Review review) {
        Review updatedReview = reviewStorage.update(review);
        feedStorage.addEvent(updatedReview.getUserId(), updatedReview.getReviewId(), "REVIEW", "UPDATE");
        return updatedReview;
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
        Review review = getById(id);
        reviewStorage.delete(id);
        feedStorage.addEvent(review.getUserId(), id, "REVIEW", "REMOVE");
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