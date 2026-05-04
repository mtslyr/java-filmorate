package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.exception.review.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;
import ru.yandex.practicum.filmorate.repository.entity.ReviewEntity;
import ru.yandex.practicum.filmorate.repository.mappers.ReviewRowMapper;
import org.springframework.http.HttpStatus;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Repository("H2ReviewStorage")
public class H2ReviewStorage extends BaseStorage<ReviewEntity> implements ReviewStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM reviews ORDER BY useful DESC";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM reviews WHERE id = ?";
    private static final String FIND_BY_FILM_ID_QUERY = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
    private static final String FIND_ALL_ORDER_BY_USEFUL = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
    private static final String INSERT_QUERY = """
            INSERT INTO reviews(content, is_positive, user_id, film_id, useful, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_QUERY = """
            UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?
            """;
    private static final String UPDATE_USEFUL_QUERY = """
            UPDATE reviews SET useful = useful + ? WHERE id = ?
            """;
    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE id = ?";

    public H2ReviewStorage(JdbcTemplate jdbc, ReviewRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Review> getAll() {
        return findMany(FIND_ALL_QUERY)
                .stream()
                .map(ReviewEntity::toReview)
                .toList();
    }

    @Override
    public Review save(Review review) {
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(java.time.LocalDateTime.now());
        }

        if (review.getUseful() == null) {
            review.setUseful(0);
        }

        try {
            update(INSERT_QUERY,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getUserId(),
                    review.getFilmId(),
                    review.getUseful(),
                    review.getCreatedAt());

            // Получаем последний ID через MAX
            Long id = jdbc.queryForObject("SELECT MAX(id) FROM reviews", Long.class);
            review.setId(id);
            return review;
        } catch (DuplicateKeyException e) {
            throw new ApiException(
                    "Пользователь уже оставил отзыв на этот фильм",
                    "userId",
                    String.valueOf(review.getUserId()),
                    HttpStatus.CONFLICT
            );
        }
    }

    @Override
    public Review update(Review review) throws ApiException {
        getById(review.getId());

        try {
            update(UPDATE_QUERY,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getId());
            return getById(review.getId());
        } catch (Exception e) {
            throw new ApiException(
                    "Ошибка при обновлении отзыва: " + e.getMessage(),
                    null,
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public Review getById(long id) {
        return findOne(FIND_BY_ID_QUERY, id)
                .orElseThrow(() -> new ReviewNotFoundException(id))
                .toReview();
    }

    @Override
    public void delete(long id) {
        getById(id);
        delete(DELETE_QUERY, id);
    }

    @Override
    public List<Review> getByFilmId(long filmId, int count) {
        return findMany(FIND_BY_FILM_ID_QUERY, filmId, count)
                .stream()
                .map(ReviewEntity::toReview)
                .toList();
    }

    @Override
    public List<Review> getAllOrderByUseful(int count) {
        return findMany(FIND_ALL_ORDER_BY_USEFUL, count)
                .stream()
                .map(ReviewEntity::toReview)
                .toList();
    }

    //Проапдейтить полезность отзыва
    @Override
    public void updateUseful(long reviewId, int delta) {
        log.info("=== updateUseful: reviewId={}, delta={} ===", reviewId, delta);

        Integer currentUseful = jdbc.queryForObject("SELECT useful FROM reviews WHERE id = ?", Integer.class, reviewId);
        log.info("Текущий useful: {}", currentUseful);

        getById(reviewId);

        int rowsUpdated = jdbc.update(UPDATE_USEFUL_QUERY, delta, reviewId);
        log.info("Обновлено строк: {}", rowsUpdated);

        // Проверяем новое значение
        Integer newUseful = jdbc.queryForObject("SELECT useful FROM reviews WHERE id = ?", Integer.class, reviewId);
        log.info("Новый useful: {}", newUseful);
    }
}