package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.ReviewReaction;
import ru.yandex.practicum.filmorate.repository.ReviewReactionStorage;
import ru.yandex.practicum.filmorate.repository.entity.ReviewReactionEntity;
import ru.yandex.practicum.filmorate.repository.mappers.ReviewReactionRowMapper;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class H2ReviewReactionStorage extends BaseStorage<ReviewReactionEntity> implements ReviewReactionStorage {

    private static final String INSERT_QUERY = """
            INSERT INTO review_reactions(review_id, user_id, is_like, created_at) 
            VALUES (?, ?, ?, ?)
            """;
    private static final String DELETE_QUERY = "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ?";
    private static final String FIND_BY_REVIEW_AND_USER_QUERY =
            "SELECT * FROM review_reactions WHERE review_id = ? AND user_id = ?";

    public H2ReviewReactionStorage(JdbcTemplate jdbc, ReviewReactionRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public ReviewReaction save(ReviewReaction reaction) {
        update(INSERT_QUERY,
                reaction.getReviewId(),
                reaction.getUserId(),
                reaction.getIsLike(),
                LocalDateTime.now());

        return reaction;
    }

    @Override
    public void delete(long reviewId, long userId) {
        delete(DELETE_QUERY, reviewId, userId);
    }

    @Override
    public Optional<ReviewReaction> findByReviewIdAndUserId(long reviewId, long userId) {
        return findOne(FIND_BY_REVIEW_AND_USER_QUERY, reviewId, userId)
                .map(ReviewReactionEntity::toReviewReaction);
    }
}