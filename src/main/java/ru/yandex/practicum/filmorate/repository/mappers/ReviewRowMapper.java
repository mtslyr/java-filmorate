package ru.yandex.practicum.filmorate.repository.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.repository.entity.ReviewEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<ReviewEntity> {
    @Override
    public ReviewEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReviewEntity review = new ReviewEntity();
        review.setId(rs.getLong("id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getLong("user_id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUseful(rs.getInt("useful"));

        if (rs.getTimestamp("created_at") != null) {
            review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }

        return review;
    }
}