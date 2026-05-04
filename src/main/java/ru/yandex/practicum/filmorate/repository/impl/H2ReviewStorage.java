package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class H2ReviewStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setInt(5, 0);
            return ps;
        }, keyHolder);

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        int updatedRows = jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        if (updatedRows == 0) throw new NotFoundException("Отзыв не найден");
        return getById(review.getReviewId());
    }

    @Override
    public Review getById(Long id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToReview, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Отзыв с id " + id + " не найден");
        }
    }

    @Override
    public List<Review> getAll(Long filmId, int count) {
        if (filmId == null) {
            String sql = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
            return jdbcTemplate.query(sql, this::mapRowToReview, count);
        } else {
            String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
            return jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        if (jdbcTemplate.update(sql, id) == 0) throw new NotFoundException("Отзыв не найден");
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        String sqlLike = "MERGE INTO review_likes (review_id, user_id, is_like) KEY(review_id, user_id) VALUES (?, ?, true)";
        jdbcTemplate.update(sqlLike, reviewId, userId);
        updateUseful(reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        String sqlLike = "MERGE INTO review_likes (review_id, user_id, is_like) KEY(review_id, user_id) VALUES (?, ?, false)";
        jdbcTemplate.update(sqlLike, reviewId, userId);
        updateUseful(reviewId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = true";
        jdbcTemplate.update(sql, reviewId, userId);
        updateUseful(reviewId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = false";
        jdbcTemplate.update(sql, reviewId, userId);
        updateUseful(reviewId);
    }

    private void updateUseful(Long reviewId) {
        String sql = "UPDATE reviews SET useful = (" +
                "SELECT (SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = true) - " +
                "(SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = false)) " +
                "WHERE review_id = ?";
        jdbcTemplate.update(sql, reviewId, reviewId, reviewId);
    }

    private void changeUseful(Long reviewId, Long userId, boolean isLike, int delta) {
        String sqlLike = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlLike, reviewId, userId, isLike);
        String sqlReview = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
        jdbcTemplate.update(sqlReview, delta, reviewId);
    }

    private void removeReaction(Long reviewId, Long userId, int delta) {
        String sqlLike = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        if (jdbcTemplate.update(sqlLike, reviewId, userId) > 0) {
            String sqlReview = "UPDATE reviews SET useful = useful - ? WHERE review_id = ?";
            jdbcTemplate.update(sqlReview, delta, reviewId);
        }
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(rs.getLong("review_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }
}