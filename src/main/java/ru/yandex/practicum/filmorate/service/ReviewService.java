package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.exception.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewReaction;
import ru.yandex.practicum.filmorate.model.request.ReviewRequest;
import ru.yandex.practicum.filmorate.model.response.ReviewResponse;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.ReviewReactionStorage;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;


import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final ReviewReactionStorage reactionStorage;
    private final ReviewMapper mapper;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public ReviewService(
            @Qualifier("H2ReviewStorage") ReviewStorage reviewStorage,
            ReviewReactionStorage reactionStorage,
            ReviewMapper mapper,
            @Qualifier("H2UserStorage") UserStorage userStorage,
            @Qualifier("H2FilmStorage") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.reactionStorage = reactionStorage;
        this.mapper = mapper;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }


    public ReviewResponse createReview(ReviewRequest request) {
        log.info("Создание отзыва: filmId={}, userId={}", request.getFilmId(), request.getUserId());

        // Проверяем существование пользователя
        try {
            userStorage.getById(request.getUserId());
        } catch (UserNotFoundException e) {
            throw new ApiException(
                    "Пользователь не найден",
                    "userId",
                    String.valueOf(request.getUserId()),
                    HttpStatus.NOT_FOUND
            );
        }

        // Проверяем существование фильма
        try {
            filmStorage.getById(request.getFilmId());
        } catch (FilmNotFoundException e) {
            throw new ApiException(
                    "Фильм не найден",
                    "filmId",
                    String.valueOf(request.getFilmId()),
                    HttpStatus.NOT_FOUND
            );
        }

        Review review = mapper.toReview(request);
        validate(review);
        review.setUseful(0);
        Review saved = reviewStorage.save(review);
        return mapper.toResponse(saved);
    }

    public ReviewResponse updateReview(ReviewRequest request) {
        log.info("Обновление отзыва: reviewId={}", request.getReviewId());  // исправлено
        reviewStorage.getById(request.getReviewId());  // исправлено
        Review review = mapper.toReview(request);
        review.setId(request.getReviewId());  // исправлено
        Review updated = reviewStorage.update(review);
        return mapper.toResponse(updated);
    }

    public ReviewResponse getReviewById(Long reviewId) {
        log.info("Получение отзыва по ID: {}", reviewId);
        Review review = reviewStorage.getById(reviewId);
        return mapper.toResponse(review);
    }

    public void deleteReview(Long reviewId) {
        log.info("Удаление отзыва: {}", reviewId);
        reviewStorage.getById(reviewId);
        reviewStorage.delete(reviewId);
    }

    public List<ReviewResponse> getReviews(Long filmId, Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }

        log.info("Получение отзывов: filmId={}, count={}", filmId, count);

        List<Review> reviews;
        if (filmId != null) {
            reviews = reviewStorage.getByFilmId(filmId, count);
        } else {
            reviews = reviewStorage.getAllOrderByUseful(count);
        }

        return reviews.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public void addLike(Long reviewId, Long userId) {
        log.info("=== addLike: reviewId={}, userId={} ===", reviewId, userId);
        reviewStorage.getById(reviewId);

        var existingReaction = reactionStorage.findByReviewIdAndUserId(reviewId, userId);
        log.info("Существующая реакция: {}", existingReaction.isPresent());

        if (existingReaction.isPresent()) {
            var reaction = existingReaction.get();
            if (!reaction.getIsLike()) {
                log.info("Меняем дизлайк на лайк");
                reactionStorage.delete(reviewId, userId);
                reactionStorage.save(createReaction(reviewId, userId, true));
                reviewStorage.updateUseful(reviewId, 2);
                log.info("Лайк добавлен (изменение)");
            } else {
                log.info("Уже есть лайк, пропускаем");
            }
        } else {
            log.info("Нет реакции, создаем новую");
            reactionStorage.save(createReaction(reviewId, userId, true));
            reviewStorage.updateUseful(reviewId, 1);
            log.info("Лайк добавлен (новый)");
        }
    }

    public void addDislike(Long reviewId, Long userId) {
        log.info("Пользователь {} ставит дизлайк отзыву {}", userId, reviewId);
        reviewStorage.getById(reviewId);

        var existingReaction = reactionStorage.findByReviewIdAndUserId(reviewId, userId);

        if (existingReaction.isPresent()) {
            var reaction = existingReaction.get();
            if (reaction.getIsLike()) {
                reactionStorage.delete(reviewId, userId);
                reactionStorage.save(createReaction(reviewId, userId, false));
                reviewStorage.updateUseful(reviewId, -2);
                log.info("Пользователь {} изменил лайк на дизлайк для отзыва {}", userId, reviewId);
            }
        } else {
            reactionStorage.save(createReaction(reviewId, userId, false));
            reviewStorage.updateUseful(reviewId, -1);
            log.info("Пользователь {} добавил дизлайк отзыву {}", userId, reviewId);
        }
    }

    public void deleteLike(Long reviewId, Long userId) {
        log.info("Пользователь {} удаляет лайк с отзыва {}", userId, reviewId);
        var reaction = reactionStorage.findByReviewIdAndUserId(reviewId, userId);

        if (reaction.isPresent() && reaction.get().getIsLike()) {
            reactionStorage.delete(reviewId, userId);
            reviewStorage.updateUseful(reviewId, -1);
            log.info("Лайк пользователя {} удален с отзыва {}", userId, reviewId);
        }
    }

    public void deleteDislike(Long reviewId, Long userId) {
        log.info("Пользователь {} удаляет дизлайк с отзыва {}", userId, reviewId);
        var reaction = reactionStorage.findByReviewIdAndUserId(reviewId, userId);

        if (reaction.isPresent() && !reaction.get().getIsLike()) {
            reactionStorage.delete(reviewId, userId);
            reviewStorage.updateUseful(reviewId, 1);
            log.info("Дизлайк пользователя {} удален с отзыва {}", userId, reviewId);
        }
    }

    //Валидатор отзыва
    private void validate(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new IllegalArgumentException("Содержание отзыва не может быть пустым");
        }
        if (review.getIsPositive() == null) {
            throw new IllegalArgumentException("Тип отзыва (isPositive) обязателен");
        }
        if (review.getUserId() == null) {
            throw new IllegalArgumentException("ID пользователя обязателен");
        }
        if (review.getFilmId() == null) {
            throw new IllegalArgumentException("ID фильма обязателен");
        }
    }

    private ReviewReaction createReaction(Long reviewId, Long userId, Boolean isLike) {
        ReviewReaction reaction = new ReviewReaction();
        reaction.setReviewId(reviewId);
        reaction.setUserId(userId);
        reaction.setIsLike(isLike);
        return reaction;
    }
}