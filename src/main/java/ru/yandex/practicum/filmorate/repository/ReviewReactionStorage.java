package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.ReviewReaction;

import java.util.Optional;

public interface ReviewReactionStorage {
    ReviewReaction save(ReviewReaction reaction);

    void delete(long reviewId, long userId);

    //Найти реакцию пользователя на отзыве
    Optional<ReviewReaction> findByReviewIdAndUserId(long reviewId, long userId);
}