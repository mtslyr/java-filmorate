package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.request.ReviewRequest;
import ru.yandex.practicum.filmorate.model.response.ReviewResponse;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;

    // Создать отзыв
    @PostMapping
    public ReviewResponse createReview(@RequestBody @Validated(OnCreate.class) ReviewRequest request) {
        log.info("POST /reviews - создание отзыва");
        return reviewService.createReview(request);
    }

    // Обновить отзыв
    @PutMapping
    public ReviewResponse updateReview(@RequestBody @Validated(OnUpdate.class) ReviewRequest request) {
        log.info("PUT /reviews - обновление отзыва с id={}", request.getReviewId());
        return reviewService.updateReview(request);
    }

    //Удалить отзыв
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable("id") Long reviewId) {
        log.info("DELETE /reviews/{} - удаление отзыва", reviewId);
        reviewService.deleteReview(reviewId);
    }

    //Получить отзыв по его id
    @GetMapping("/{id}")
    public ReviewResponse getReviewById(@PathVariable("id") Long reviewId) {
        log.info("GET /reviews/{} - получение отзыва по id", reviewId);
        return reviewService.getReviewById(reviewId);
    }

    //Получить отзывы по филиьмы с ограничением по кол-ву. Если ограничение не указано, то выдаст 10 шт
    @GetMapping
    public List<ReviewResponse> getReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(required = false, defaultValue = "10") Integer count) {
        log.info("GET /reviews - получение отзывов: filmId={}, count={}", filmId, count);
        return reviewService.getReviews(filmId, count);
    }

    //Добавить лайк
    @PutMapping("/{id}/like/{userId}")
    public void addLike(
            @PathVariable("id") Long reviewId,
            @PathVariable("userId") Long userId) {
        log.info("PUT /reviews/{}/like/{} - добавление лайка", reviewId, userId);
        reviewService.addLike(reviewId, userId);
    }

    //Добавить дизлайк
    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(
            @PathVariable("id") Long reviewId,
            @PathVariable("userId") Long userId) {
        log.info("PUT /reviews/{}/dislike/{} - добавление дизлайка", reviewId, userId);
        reviewService.addDislike(reviewId, userId);
    }

    //Удалить лайк
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(
            @PathVariable("id") Long reviewId,
            @PathVariable("userId") Long userId) {
        log.info("DELETE /reviews/{}/like/{} - удаление лайка", reviewId, userId);
        reviewService.deleteLike(reviewId, userId);
    }

    //Удалить дизлайк
    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(
            @PathVariable("id") Long reviewId,
            @PathVariable("userId") Long userId) {
        log.info("DELETE /reviews/{}/dislike/{} - удаление дизлайка", reviewId, userId);
        reviewService.deleteDislike(reviewId, userId);
    }
}