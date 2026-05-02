package ru.yandex.practicum.filmorate.repository.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.repository.entity.ReviewReactionEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewReactionRowMapper implements RowMapper<ReviewReactionEntity> {
    @Override
    public ReviewReactionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReviewReactionEntity reaction = new ReviewReactionEntity();
        reaction.setId(rs.getLong("id"));
        reaction.setReviewId(rs.getLong("review_id"));
        reaction.setUserId(rs.getLong("user_id"));
        reaction.setIsLike(rs.getBoolean("is_like"));

        if (rs.getTimestamp("created_at") != null) {
            reaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }

        return reaction;
    }
}